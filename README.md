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

# 🚀 First Time Setup

Clone repository:

```bash
git clone https://github.com/BaoVy2810/TEATRACK_MCD_253EIE502802_Group02.git

cd TEATRACK_MCD_253EIE502802_Group02
```

Tạo branch cá nhân (chỉ làm một lần):

### Bảo Vy

```bash
git checkout -b BaoVy
git push -u origin BaoVy
```

### Thanh Thanh

```bash
git checkout -b ThanhThanh
git push -u origin ThanhThanh
```

### Hồng Hạnh

```bash
git checkout -b HongHanh
git push -u origin HongHanh
```

### Hoàng Đức

```bash
git checkout -b HoangDuc
git push -u origin HoangDuc
```

### Trung Nhân

```bash
git checkout -b TrungNhan
git push -u origin TrungNhan
```

---

# 👨‍💻 Daily Workflow

Mỗi lần bắt đầu làm việc:

1. Cập nhật `main`
2. Chuyển sang branch cá nhân
3. Đồng bộ branch cá nhân với `main`
4. Coding
5. Commit
6. Push lên branch cá nhân

---

# 🍵 Quick Copy For Each Member

## Dành cho Bảo Vy

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

## Dành cho Thanh Thanh

```bash
git checkout main
git pull origin main

git checkout ThanhThanh

# Coding...

git add .
git commit -m "Update login screen"
git push origin ThanhThanh
```

---

## Dành cho Hồng Hạnh

```bash
git checkout main
git pull origin main

git checkout HongHanh

# Coding...

git add .
git commit -m "Update about us screen"
git push origin HongHanh
```

---

## Dành cho Hoàng Đức

```bash
git checkout main
git pull origin main

git checkout HoangDuc

# Coding...

git add .
git commit -m "Update admin dashboard"
git push origin HoangDuc
```

---

## Dành cho Trung Nhân

```bash
git checkout main
git pull origin main

git checkout TrungNhan

# Coding...

git add .
git commit -m "Update promotion management"
git push origin TrungNhan
```

---

# 🔀 Merge Workflow

Sau khi hoàn thành chức năng:

* Thành viên chỉ cần push lên branch cá nhân.
* Báo Leader để merge vào `main`.

Ví dụ:

> "Em đã push branch của em xong, nhờ Leader merge giúp."

---

## Leader Merge

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

---

# ✅ Pull Request (Optional)

Nếu nhóm muốn theo workflow chuẩn GitHub:

1. Push branch cá nhân.
2. Vào GitHub Repository.
3. Chọn **Compare & Pull Request**.
4. Base Branch = `main`.
5. Compare Branch = branch cá nhân.
6. Leader review và merge.

Ví dụ:

```text
main ← BaoVy
```

> Đối với đồ án môn học, Pull Request không bắt buộc nếu Leader merge thủ công.

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

✅ Merge `main` vào branch cá nhân trước khi bắt đầu

✅ Commit message rõ ràng

✅ Push lên branch cá nhân

✅ Báo Leader sau khi hoàn thành chức năng

✅ Đồng bộ branch với `main` thường xuyên

❌ Không push trực tiếp lên `main`

❌ Không code trên `main`

❌ Không sửa module của thành viên khác khi chưa trao đổi

❌ Không commit file build, APK hoặc thư mục `build/`

---

# 🎯 Team Goal

* Hoàn thành đúng chức năng được phân công.
* Hạn chế conflict khi merge code.
* Tuân thủ Git Workflow của nhóm.
* Giữ cấu trúc project rõ ràng, dễ bảo trì.
