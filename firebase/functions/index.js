const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

function getSmtpConfig() {
  const legacyConfig = functions.config().teatrack || {};
  return {
    user: process.env.SMTP_USER || legacyConfig.email_user,
    pass: process.env.SMTP_PASS || legacyConfig.email_pass,
  };
}

function createTransporter() {
  const smtp = getSmtpConfig();
  if (!smtp.user || !smtp.pass) {
    throw new Error("Missing SMTP_USER/SMTP_PASS or teatrack.email_user/teatrack.email_pass");
  }

  return nodemailer.createTransport({
    service: "gmail",
    auth: {
      user: smtp.user,
      pass: smtp.pass,
    },
  });
}

exports.sendPasswordResetOtpEmail = functions.database
  .ref("/otp/{otpId}")
  .onWrite(async (change) => {
    if (!change.after.exists()) {
      return null;
    }

    const before = change.before.exists() ? change.before.val() || {} : {};
    const request = change.after.val() || {};
    if (request.status !== "pending" || before.status === "pending") {
      return null;
    }

    const to = request.to || request.email;
    if (!to || !request.subject || !request.html) {
      await change.after.ref.update({
        status: "failed",
        error: "Missing email request data",
        updatedAt: Date.now(),
      });
      return null;
    }

    if (request.expiresAt && Number(request.expiresAt) <= Date.now()) {
      await change.after.ref.update({
        status: "expired",
        updatedAt: Date.now(),
      });
      return null;
    }

    try {
      const smtp = getSmtpConfig();
      const transporter = createTransporter();
      await transporter.sendMail({
        from: `"TeaTrack Support" <${smtp.user}>`,
        to,
        subject: request.subject,
        html: request.html,
      });

      await change.after.ref.update({
        status: "sent",
        sentAt: Date.now(),
        updatedAt: Date.now(),
      });
    } catch (error) {
      await change.after.ref.update({
        status: "failed",
        error: error.message,
        updatedAt: Date.now(),
      });
    }
    return null;
  });
