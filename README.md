# TEATRACK — Ứng dụng Đặt Trà Sữa (Mobile Commerce Development)

**Mã nhóm:** 253EIE502802 — Group 02  
**Môn học:** Mobile Commerce Development (MCD)  
**Package:** `com.teatrack_mcd_253eie502802_group02`  
**Repository:** https://github.com/BaoVy2810/TEATRACK_MCD_253EIE502802_Group02

---

## 1. Tên đồ án & Mô tả tổng quan

**TEATRACK** (thương hiệu **Hồng Trà Ngô Gia**) là ứng dụng Android thương mại di động cho chuỗi đồ uống trà sữa, trà trái cây. Ứng dụng gồm hai vai trò chính:

| Vai trò | Mô tả |
|---------|--------|
| **Client (Khách hàng)** | Duyệt menu, đặt hàng, thanh toán, tích điểm, nhận khuyến mãi, theo dõi đơn, chatbot AI |
| **Admin (Quản trị)** | Dashboard thống kê, quản lý sản phẩm, đơn hàng, khuyến mãi, chi nhánh, blog, phản hồi |

Dữ liệu được lưu trữ trên **Firebase Realtime Database**; xác thực qua **Firebase Authentication** (email/password + Google Sign-In).

---

## 2. Thành viên nhóm

| STT | Họ tên               | Branch GitHub |
|-----|----------------------|---------------|
| 1 | Trần Ngọc Bảo Vy     | `BaoVy` |
| 2 | Nguyễn Thanh Thanh   | `ThanhThanh` |
| 3 | Nguyễn Thị Hồng Hạnh | `HongHanh` |
| 4 | Nguyễn Hoàng Đức     | `HoangDuc` |
| 5 | Lê Trung Nhân        | `TrungNhan` |

> Mỗi thành viên làm việc trên branch riêng, merge vào `main` qua Leader.

---

## 3. Công nghệ sử dụng

### Nền tảng & Ngôn ngữ
- **Android (Java)** — `minSdk 26`, `targetSdk 35`, `compileSdk 37`
- **Gradle 9.3.1** + **Android Gradle Plugin 9.1.1**
- **View Binding**, **Material Design Components**
- **Java 11**

### Backend & Cloud
| Công nghệ | Mục đích |
|-----------|----------|
| **Firebase Authentication** | Đăng nhập email/password, Google Sign-In |
| **Firebase Realtime Database** | Users, products, orders, vouchers, blogs, agencies, contacts, reviews, points |
| **Firebase Storage** | Ảnh chi nhánh (agency) |
| **Firebase Analytics** | Theo dõi sử dụng (tùy chọn) |
| **Google Credential Manager** | Đăng nhập Google hiện đại |
| **Cloudinary** | Upload ảnh sản phẩm & khuyến mãi (unsigned preset) |

### Thư viện bên thứ ba
| Thư viện | Mục đích |
|----------|----------|
| **Glide** | Tải & hiển thị ảnh từ URL |
| **MPAndroidChart** | Biểu đồ Dashboard Admin (Line, Bar, Pie) |
| **ZXing** | Tạo mã QR voucher/khuyến mãi |
| **Google Generative AI (Gemini)** | Chatbot trợ lý ảo TeaTrack Assistant |
| **CircleImageView** | Avatar người dùng |
| **Navigation Component** | Điều hướng (nếu dùng) |

---

## 4. Cấu trúc thư mục chính

```
TEATRACK_MCD_253EIE502802_Group02/
├── app/                          # Module Android chính
│   ├── build.gradle.kts          # Dependencies & cấu hình build
│   ├── google-services.json      # Cấu hình Firebase
│   └── src/main/
│       ├── AndroidManifest.xml   # Khai báo Activity & quyền
│       ├── java/com/teatrack_mcd_253eie502802_group02/
│       │   ├── MainActivity.java
│       │   ├── TeaTrackApplication.java
│       │   ├── client/           # Màn hình phía khách hàng (~35 Activity)
│       │   ├── admin/            # Màn hình quản trị (~12 Activity)
│       │   ├── adapter/          # RecyclerView adapters
│       │   ├── data/             # Repository, Order flow, Password reset
│       │   ├── model/            # POJO: User, Product, Order, Promotion...
│       │   ├── shared/           # BaseActivity, QR, Locale, UI helpers
│       │   └── util/             # Session, avatar, pricing, role helpers
│       └── res/
│           ├── layout/           # XML layouts
│           ├── values/           # strings.xml (EN)
│           └── values-vi/        # strings.xml (VI) — đa ngôn ngữ
├── firebase/
│   └── orders_seed.json          # Dữ liệu mẫu đơn hàng
├── gradle/
│   └── libs.versions.toml        # Version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

### Cấu trúc Firebase Realtime Database

| Node | Mô tả |
|------|--------|
| `Users` | Tài khoản (role: Admin / Customer / Customer VIP) |
| `products` | Sản phẩm (danh mục, giá, topping, ảnh) |
| `orders` | Đơn hàng |
| `vouchers` | Mã khuyến mãi / voucher |
| `blogs` | Bài viết tin tức |
| `agencies` | Chi nhánh cửa hàng |
| `contacts` | Phản hồi / khiếu nại từ khách |
| `reviews` | Đánh giá sản phẩm |
| `PointsHistory` | Lịch sử tích điểm |
| `otp` | Mã OTP đặt lại mật khẩu |
| `appConfig` | Cấu hình ứng dụng (mail server URL...) |

**Database URL:** `https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app`  
**Firebase Project ID:** `teatrack-htng`

---

## 5. Chức năng phía Client (Khách hàng)

### Xác thực & Tài khoản
- Đăng ký, đăng nhập (email/username + password)
- **Đăng nhập Google** (Credential Manager + fallback legacy)
- Ghi nhớ mật khẩu
- Quên mật khẩu qua **OTP email** (`PasswordResetManager`)
- Phân quyền tự động: Admin → Dashboard; Customer → Homepage

### Mua hàng
- **Homepage:** Banner, sản phẩm nổi bật, tin tức, khuyến mãi
- **Menu:** Danh sách sản phẩm theo danh mục
- **ProductDetail:** Chi tiết món, topping, mức đường/đá, đánh giá
- **Cart:** Giỏ hàng, chọn chi nhánh, áp voucher
- **Checkout & Payment:** Thanh toán (Cash/Bank, MoMo, ZaloPay, e-wallet — mô phỏng)
- **OrderTracking:** Theo dõi trạng thái đơn realtime
- **OrderHistory / OrderDetails:** Lịch sử & chi tiết đơn

### Loyalty & Khuyến mãi
- Tích điểm thành viên (Bronze / Silver / Gold)
- **MyRewardsActivity:** Voucher đã nhận
- **PromotionClient:** Danh sách khuyến mãi + **mã QR**
- **EarnedPointHistoryActivity:** Lịch sử điểm

### Hồ sơ & Cài đặt
- **UserProfile:** Thông tin cá nhân, avatar
- **PersonalInformationActivity / EditingPerInfoActivity:** Sửa hồ sơ
- **ChangeLanguageActivity:** Chuyển **Tiếng Việt / English**
- **MyReviewsActivity:** Đánh giá của tôi

### Nội dung & Hỗ trợ
- **BlogGeneral / BlogDetail:** Tin tức & blog
- **AboutUsActivity:** Giới thiệu thương hiệu (video, animation)
- **Agency:** Danh sách chi nhánh
- **ContactWithUs:** Gửi phản hồi/khiếu nại theo chi nhánh & chủ đề
- **ChatbotBubble:** Trợ lý AI **Gemini** (tư vấn menu, đa ngôn ngữ VI/EN)
- Chính sách: Terms, Privacy, Membership, Refund

---

## 6. Chức năng phía Admin (Quản trị)

| Màn hình | Chức năng |
|----------|-----------|
| **AdminDashboard** | Thống kê doanh thu, biểu đồ Line/Bar/Pie, đơn gần đây, phân tích chi nhánh |
| **AdminProduct** | CRUD sản phẩm, upload ảnh Cloudinary |
| **AdminOrders** | Quản lý đơn (lọc: pending, processing, shipping, completed, cancelled) |
| **AdminOrderDetailActivity** | Chi tiết & cập nhật trạng thái đơn |
| **AdminPromotion** | CRUD voucher/khuyến mãi, upload ảnh |
| **AdminAgency** | Quản lý chi nhánh, ảnh Firebase Storage |
| **AdminBlog / AdminBlogDetail** | Quản lý bài viết blog |
| **AdminComplaints** | Xử lý phản hồi (All / Unread / Pending / Resolved) |
| **AdminAccount** | Quản lý tài khoản người dùng (role, status) |
| **AdminProfile** | Hồ sơ admin |

---

## 7. Cách build và chạy app

### Yêu cầu
- **Android Studio** (khuyến nghị phiên bản hỗ trợ AGP 9.x)
- **JDK 11+**
- Thiết bị/emulator **Android 8.0+** (API 26+)
- Tài khoản Firebase có quyền truy cập project `teatrack-htng`

### Các bước

1. **Clone repository**
   ```bash
   git clone https://github.com/BaoVy2810/TEATRACK_MCD_253EIE502802_Group02.git
   cd TEATRACK_MCD_253EIE502802_Group02
   ```

2. **Cấu hình `local.properties`** (tự tạo tại thư mục gốc nếu chưa có)
   ```properties
   sdk.dir=/path/to/Android/sdk
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   > `GEMINI_API_KEY` cần thiết cho tính năng Chatbot (Gemini AI).

3. **Firebase:** Đảm bảo file `app/google-services.json` tồn tại (đã có trong repo).

4. **Mở project** trong Android Studio → Sync Gradle.

5. **Chạy app**
   ```bash
   ./gradlew :app:assembleDebug
   ```

### Tài khoản demo
> Liên hệ nhóm hoặc xem Firebase Console để lấy tài khoản test Admin/Customer.

---

## 8. Cấu hình Firebase

### `google-services.json`
- Đặt tại: `app/google-services.json`
- **Package name** phải khớp: `com.teatrack_mcd_253eie502802_group02`
- **Project ID:** `teatrack-htng`

### SHA-1 fingerprint (bắt buộc cho Google Sign-In)

Lấy SHA-1 debug keystore:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Thêm SHA-1 vào **Firebase Console → Project Settings → Your apps → Android app → Add fingerprint**.

> Nếu đăng nhập Google lỗi trên máy mới, cần thêm SHA-1 của máy đó.

### Firebase Services đã tích hợp
- Authentication (Email + Google)
- Realtime Database (region: `asia-southeast1`)
- Storage (`teatrack-htng.firebasestorage.app`)
- Analytics

### Offline persistence
`TeaTrackApplication` bật `setPersistenceEnabled(true)` và `keepSynced` cho node `products`.

---

## 9. Tính năng nổi bật

| Tính năng | Mô tả |
|-----------|--------|
| **Đa ngôn ngữ VI/EN** | `values/` + `values-vi/`, `LocaleHelper`, đổi ngôn ngữ trong Profile |
| **Google Sign-In** | Credential Manager API + Firebase Auth |
| **Chatbot AI (Gemini)** | Tư vấn menu Ngô Gia, hỗ trợ VI/EN, system prompt chuyên biệt |
| **Hệ thống Loyalty** | Điểm thưởng, hạng thành viên, lịch sử điểm |
| **Khuyến mãi & QR Code** | Voucher, mã QR (ZXing) |
| **Dashboard Admin** | Biểu đồ MPAndroidChart, lọc theo thời gian |
| **Đánh giá sản phẩm** | Review sync, thống kê rating |
| **Phản hồi/Khiếu nại** | Client gửi → Admin xử lý theo trạng thái |
| **Upload ảnh** | Cloudinary (sản phẩm, KM) + Firebase Storage (chi nhánh) |
| **OTP Reset Password** | Gửi email OTP qua mail server (cấu hình trong `appConfig`) |

---

## 10. Lưu ý cho giảng viên

- Ứng dụng mô phỏng thanh toán (không tích hợp cổng thanh toán thật).
- Chatbot cần API key Gemini hợp lệ trong `local.properties`.
- Dữ liệu realtime phụ thuộc Firebase project `teatrack-htng`.
- Một số tính năng email OTP cần mail server (mặc định `http://10.0.2.2:3000` trên emulator).

---

## 11. Git Workflow (nội bộ nhóm)

> ⚠️ Không code trực tiếp trên `main`.

### First Time Setup

```bash
git clone https://github.com/BaoVy2810/TEATRACK_MCD_253EIE502802_Group02.git
cd TEATRACK_MCD_253EIE502802_Group02
```

Tạo branch cá nhân (chỉ làm một lần):

| Thành viên | Lệnh |
|------------|------|
| Bảo Vy | `git checkout -b BaoVy && git push -u origin BaoVy` |
| Thanh Thanh | `git checkout -b ThanhThanh && git push -u origin ThanhThanh` |
| Hồng Hạnh | `git checkout -b HongHanh && git push -u origin HongHanh` |
| Hoàng Đức | `git checkout -b HoangDuc && git push -u origin HoangDuc` |
| Trung Nhân | `git checkout -b TrungNhan && git push -u origin TrungNhan` |

### Daily Workflow

1. Cập nhật `main`
2. Chuyển sang branch cá nhân
3. Đồng bộ branch cá nhân với `main`
4. Coding → Commit → Push lên branch cá nhân
5. Báo Leader merge vào `main`

### Leader Merge

```bash
git checkout main
git pull origin main
git merge BaoVy
git merge ThanhThanh
git merge HongHanh
git merge HoangDuc
git merge TrungNhan
git push origin main
```

### Commit Message Convention

| Action | Example |
|--------|---------|
| Add Feature | Add product detail screen |
| Update Feature | Update checkout screen |
| Fix Bug | Fix login validation |
| Refactor | Refactor API service |
| UI Change | Update home page UI |

### Rules

✅ Luôn làm việc trên branch cá nhân  
✅ Pull `main` trước khi code  
✅ Commit message rõ ràng  
✅ Báo Leader sau khi hoàn thành chức năng  

❌ Không push trực tiếp lên `main`  
❌ Không commit file build, APK hoặc thư mục `build/`

---

**Phiên bản app:** 1.0 (`versionCode 1`)  
**Cập nhật README:** 07/2026
