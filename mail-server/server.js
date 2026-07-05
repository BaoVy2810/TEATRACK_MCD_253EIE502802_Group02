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

  if (!user || !pass) {
    throw new Error("Missing SMTP_USER or SMTP_PASS in mail-server/.env");
  }

  return nodemailer.createTransport({
    service: "gmail",
    auth: { user, pass },
  });
}

app.get("/health", (req, res) => {
  res.json({ ok: true, service: "teatrack-otp-mail-server" });
});

app.post("/api/auth/send-otp-email", async (req, res) => {
  const { to, subject, html } = req.body || {};
  console.log(`[${new Date().toISOString()}] Nhận yêu cầu gửi OTP đến: ${to}`);

  try {
    if (!to || !subject || !html) {
      console.error("Thiếu dữ liệu email trong request body");
      return res.status(400).json({ message: "Missing email data" });
    }

    const smtpUser = process.env.SMTP_USER;
    const transporter = createTransporter();

    await transporter.sendMail({
      from: `"TeaTrack Support" <${smtpUser}>`,
      to,
      subject,
      html,
    });

    console.log(`[${new Date().toISOString()}] Gửi mail THÀNH CÔNG tới: ${to}`);
    return res.json({ message: "OTP email sent" });
  } catch (error) {
    console.error(`[${new Date().toISOString()}] Gửi mail THẤT BẠI:`, error);
    return res.status(500).json({
      message: "Failed to send OTP email",
      error: error.message,
    });
  }
});

app.listen(port, "0.0.0.0", () => {
  console.log(`TeaTrack OTP mail server is running on http://localhost:${port}`);
});
