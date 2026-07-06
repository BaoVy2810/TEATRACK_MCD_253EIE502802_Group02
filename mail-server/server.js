const cors = require("cors");
const dotenv = require("dotenv");
const express = require("express");
const nodemailer = require("nodemailer");

dotenv.config();

const app = express();
const port = Number(process.env.PORT || 3000);

app.use(cors());
app.use(express.json({ limit: "1mb" }));

function createTransporter() {
  const user = process.env.SMTP_USER;
  const pass = process.env.SMTP_PASS;
  const host = process.env.SMTP_HOST || "smtp.gmail.com";
  const port = Number(process.env.SMTP_PORT || 587);
  const secure = String(process.env.SMTP_SECURE || "false").toLowerCase() === "true";

  if (!user || !pass) {
    throw new Error("Missing SMTP_USER or SMTP_PASS in mail-server/.env");
  }

  return nodemailer.createTransport({
    host,
    port,
    secure,
    requireTLS: !secure,
    auth: { user, pass },
    connectionTimeout: 30_000,
    greetingTimeout: 30_000,
    socketTimeout: 30_000,
  });
}

async function sendWithBrevo({ to, subject, html }) {
  const apiKey = process.env.BREVO_API_KEY;
  const senderEmail = process.env.MAIL_FROM || process.env.SMTP_USER;
  const senderName = process.env.MAIL_FROM_NAME || "TeaTrack Support";

  if (!apiKey) {
    throw new Error("Missing BREVO_API_KEY");
  }
  if (!senderEmail) {
    throw new Error("Missing MAIL_FROM or SMTP_USER");
  }

  const response = await fetch("https://api.brevo.com/v3/smtp/email", {
    method: "POST",
    headers: {
      accept: "application/json",
      "api-key": apiKey,
      "content-type": "application/json",
    },
    body: JSON.stringify({
      sender: { name: senderName, email: senderEmail },
      to: [{ email: to }],
      subject,
      htmlContent: html,
    }),
  });

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(`Brevo returned HTTP ${response.status}: ${errorBody}`);
  }
}

async function sendEmail({ to, subject, html }) {
  if (process.env.BREVO_API_KEY) {
    await sendWithBrevo({ to, subject, html });
    return "brevo";
  }

  const smtpUser = process.env.SMTP_USER;
  const transporter = createTransporter();

  await transporter.sendMail({
    from: `"TeaTrack Support" <${smtpUser}>`,
    to,
    subject,
    html,
  });

  return "smtp";
}

app.get("/health", (req, res) => {
  res.json({ ok: true, service: "teatrack-otp-mail-server" });
});

app.post("/api/auth/send-otp-email", async (req, res) => {
  const { to, subject, html } = req.body || {};
  console.log(`[${new Date().toISOString()}] Received OTP email request for: ${to}`);

  try {
    if (!to || !subject || !html) {
      console.error("Missing email data in request body");
      return res.status(400).json({ message: "Missing email data" });
    }

    const provider = await sendEmail({ to, subject, html });

    console.log(`[${new Date().toISOString()}] OTP email sent to: ${to} via ${provider}`);
    return res.json({ message: "OTP email sent", provider });
  } catch (error) {
    console.error(`[${new Date().toISOString()}] Failed to send OTP email:`, error);
    return res.status(500).json({
      message: "Failed to send OTP email",
      error: error.message,
    });
  }
});

app.listen(port, "0.0.0.0", () => {
  console.log(`TeaTrack OTP mail server is running on http://localhost:${port}`);
});
