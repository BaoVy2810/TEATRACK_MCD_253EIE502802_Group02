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
    // Disable this function because we use a separate mail-server on Render
    // and Spark plan doesn't allow outbound networking for SMTP.
    return null;
  });
