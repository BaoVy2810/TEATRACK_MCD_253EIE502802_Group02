# 🍵 TEATRACK - Mobile Commerce Development

## Group 02

Repository:

https://github.com/BaoVy2810/TEATRACK_MCD_253EIE502802_Group02

---

# 📌 Branch Structure

| Member      | Branch     |
| ----------- | ---------- |
| Bảo Vy      | BaoVy      |
| Thanh Thanh | ThanhThanh |
| Hồng Hạnh   | HongHanh   |
| Hoàng Đức   | HoangDuc   |
| Trung Nhân  | TrungNhan  |

> ⚠️ Không code trực tiếp trên `main`.

---

# 📂 Project Structure

```text
app/src/main/java/com/teatrack/

├── client/
│   ├── auth/
│   ├── home/
│   ├── product/
│   ├── checkout/
│   ├── profile/
│   ├── tracking/
│   ├── content/
│   └── error/
│
├── admin/
│   ├── auth/
│   ├── dashboard/
│   ├── account/
│   ├── product/
│   ├── content/
│   ├── support/
│   └── error/
│
└── shared/
    ├── model/
    ├── api/
    ├── utils/
    └── ui/

app/src/main/res/

├── drawable/
│   └── icons/
│
├── layout/
│   ├── client/
│   └── admin/
│
└── values/
    ├── colors_client.xml
    ├── colors_admin.xml
    └── styles.xml
```

---

# 👨‍💻 Task Assignment

| Folder          | Member      | Responsibility                        |
| --------------- | ----------- | ------------------------------------- |
| client/auth     | Thanh Thanh | Login, Register, Forgot Password, OTP |
| client/profile  | Thanh Thanh | User Profile, Order History           |
| client/tracking | Thanh Thanh | Order Tracking                        |
| client/home     | Bảo Vy      | Home Page, Menu                       |
| client/product  | Bảo Vy      | Product Detail, Cart                  |
| client/checkout | Bảo Vy      | Checkout, Payment                     |
| client/content  | Hồng Hạnh   | About Us, Agency, Blog, Contact       |
| client/error    | Hồng Hạnh   | Client 404                            |
| shared/ui       | Hồng Hạnh   | Header, Footer, Chatbot               |
| admin/auth      | Hoàng Đức   | Admin Login                           |
| admin/dashboard | Hoàng Đức   | Dashboard                             |
| admin/account   | Hoàng Đức   | Admin Account & Profile               |
| admin/product   | Hoàng Đức   | Product Management, Orders            |
| admin/content   | Trung Nhân  | Agency, Blog, Promotion               |
| admin/support   | Trung Nhân  | Contact & Complaints                  |
| admin/error     | Trung Nhân  | Admin 404                             |
| shared/model    | Bảo Vy      | Models                                |
| shared/api      | Bảo Vy      | Retrofit, API Services                |
| shared/utils    | Bảo Vy      | Utilities, Constants                  |

---

# 🚀 Clone Repository

```bash
git clone https://github.com/BaoVy2810/TEATRACK_MCD_253EIE502802_Group02.git

cd TEATRACK_MCD_253EIE502802_Group02
```

---

# 🌱 Create Personal Branch (First Time Only)

Ví dụ với Bảo Vy:

```bash
git checkout -b BaoVy
git push -u origin BaoVy
```

Ví dụ với Hoàng Đức:

```bash
git checkout -b HoangDuc
git push -u origin HoangDuc
```

Ví dụ với Thanh Thanh:

```bash
git checkout -b ThanhThanh
git push -u origin ThanhThanh
```

Ví dụ với Hồng Hạnh:

```bash
git checkout -b HongHanh
git push -u origin HongHanh
```

Ví dụ với Trung Nhân:

```bash
git checkout -b TrungNhan
git push -u origin TrungNhan
```

---

# 🔄 Update Latest Code

Trước khi bắt đầu code mỗi ngày:

```bash
git checkout main
git pull origin main
```

---

# 🌿 Switch To Your Branch

Ví dụ:

```bash
git checkout BaoVy
```

---

# 🔁 Sync Branch With Main

Đồng bộ branch cá nhân với nhánh chính:

```bash
git checkout main
git pull origin main

git checkout BaoVy
git merge main
```

Nếu có conflict:

```bash
git add .
git commit -m "Resolve merge conflict"
```

---

# 💻 Coding

Sau khi đã sync branch:

```bash
git checkout BaoVy
```

Bắt đầu code các chức năng được phân công.

---

# ✅ Commit Changes

Kiểm tra file thay đổi:

```bash
git status
```

Add toàn bộ file:

```bash
git add .
```

Commit:

```bash
git commit -m "Add product detail screen"
```

Ví dụ:

```bash
git commit -m "Update checkout screen"
git commit -m "Add admin dashboard"
git commit -m "Fix login validation"
```

---

# ☁️ Push To Personal Branch

Ví dụ:

```bash
git push origin BaoVy
```

---

# 🔀 Create Pull Request

Sau khi hoàn thành chức năng:

1. Push branch lên GitHub
2. Vào repository
3. Chọn **Compare & Pull Request**
4. Base Branch = `main`
5. Compare Branch = branch cá nhân
6. Tạo Pull Request

Ví dụ:

```text
main ← BaoVy
```

---

# 🔒 Merge To Main

Chỉ Leader hoặc người được phân quyền thực hiện.

```bash
git checkout main
git pull origin main

git merge BaoVy

git push origin main
```

Hoặc merge trực tiếp trên GitHub thông qua Pull Request.

---

# ⚡ Quick Workflow

```bash
git checkout main
git pull origin main

git checkout BaoVy

# Coding...

git add .
git commit -m "Update home screen"
git push origin BaoVy
```

---

# 📋 Commit Message Convention

| Action         | Example                   |
| -------------- | ------------------------- |
| Add Feature    | Add product detail screen |
| Update Feature | Update checkout screen    |
| Fix Bug        | Fix login validation      |
| Refactor       | Refactor API service      |
| UI Change      | Update home page UI       |

---

# 📌 Rules

✅ Luôn làm việc trên branch cá nhân

✅ Pull `main` trước khi code

✅ Commit message rõ ràng

✅ Push lên branch cá nhân

✅ Tạo Pull Request trước khi merge

✅ Đặt đúng package và folder được phân công

✅ Đồng bộ branch với `main` thường xuyên

❌ Không push trực tiếp lên `main`

❌ Không code trên `main`

❌ Không sửa module của thành viên khác khi chưa trao đổi

❌ Không đổi cấu trúc project khi chưa thống nhất nhóm

❌ Không commit file build hoặc APK

---

# 🎯 Team Goal

* Hoàn thành đúng chức năng được phân công
* Hạn chế conflict khi merge code
* Tuân thủ Git Workflow của nhóm
* Giữ cấu trúc project rõ ràng, dễ bảo trì
