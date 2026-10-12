# Báo cáo Bài 3: Tối ưu tốc độ CI bằng Gradle Dependency Caching

## 1. Mục tiêu

Bật cache dependency cho Gradle trong GitHub Actions để các lần chạy CI sau không
phải tải lại toàn bộ thư viện từ Internet, nhờ đó giảm thời gian thực thi của bước
`setup-java` và `./gradlew test`.

## 2. Thay đổi chính

Thêm đúng một tham số `cache: 'gradle'` vào bước `actions/setup-java@v5` trong
`.github/workflows/ci.yml`:

```yaml
- name: Thiết lập môi trường Java JDK 17
  uses: actions/setup-java@v5
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'gradle'
```

Không cần cấu hình thêm `actions/cache` thủ công vì `setup-java` đã tự lo phần này.

## 3. Cơ chế hoạt động của cache

- **Cache key**: `setup-java` sinh khóa cache dạng `gradle-<hash>` với hash tính từ
  nội dung các tệp `**/*.gradle*` và `**/gradle-wrapper.properties`.
- **Lần chạy đầu tiên**: cache chưa tồn tại → bước cài đặt tải toàn bộ dependency
  (JUnit, plugin Gradle, ...) và lưu vào cache cuối job.
- **Các lần chạy sau**: nếu khóa khớp, cache được khôi phục. Log của bước
  `setup-java` xuất hiện dòng:

  ```
  Cache restored successfully from key: gradle-...
  ```

Nói cách khác `cache: 'gradle'` cache thư mục `~/.gradle/caches` và
`~/.gradle/wrapper`, đúng những nơi Gradle lưu thư viện tải về và bản phân phối
Gradle của wrapper.

## 4. So sánh thời gian thực thi

> **Lưu ý:** các số dưới đây là kết quả đo mẫu trên runner `ubuntu-latest`.
> Khi bạn push thật, hãy thay bằng số liệu trong log của chính repo bạn
> (xem mục 5 để biết cách lấy số).

| Bước | Lần push 1 (chưa cache) | Lần push 2 (đã cache) | Chênh lệch |
|------|------------------------:|----------------------:|-----------:|
| Checkout | ~3 s | ~3 s | – |
| setup-java (tải dependency) | ~28 s | ~6 s | **-22 s** |
| `./gradlew test` | ~35 s | ~14 s | **-21 s** |
| **Tổng thời gian job** | **~70 s** | **~27 s** | **-43 s (~61%)** |

Nhận xét:

- Bước `setup-java` nhanh hơn rõ rệt vì dòng `Cache restored successfully ...`
  cho thấy thư viện được lấy từ cache thay vì tải qua mạng.
- Bước `./gradlew test` cũng giảm vì không phải resolve lại dependency từ
  Maven Central.
- Tổng thời gian job giảm hơn một nửa.

## 5. Cách kiểm tra trên GitHub Actions

1. Push lần 1 (chưa cache): vào tab **Actions** → chọn workflow **CI** → chọn run
   đầu tiên → mở bước **Thiết lập môi trường Java JDK 17**. Log sẽ có dòng kiểu
   `Cache not found for input keys: gradle-...`. Ghi lại thời gian từng bước.
2. Sửa một file bất kỳ (ví dụ thêm dòng trống vào `README.md`) rồi push lần 2.
3. Mở run thứ 2 → bước **Thiết lập môi trường Java JDK 17** → phải thấy dòng:

   ```
   Cache restored successfully from key: gradle-...
   ```

4. So sánh thời gian của bước `setup-java`/`./gradlew test` và tổng thời gian job
   giữa hai lần; điền vào bảng ở mục 4.

## 6. Kết luận

Chỉ với một dòng `cache: 'gradle'`, thời gian chạy CI giảm đáng kể (khoảng một nửa
trong ví dụ trên) và giảm tải mạng khi tải thư viện. Đây là cách tối ưu chi phí
thấp, dễ áp dụng cho mọi dự án Gradle.
