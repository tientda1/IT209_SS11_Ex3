# Bài 3: Tối ưu tốc độ CI bằng Gradle Dependency Caching

Bật cache dependency cho Gradle trong GitHub Actions để giảm thời gian tải
thư viện từ Internet mỗi lần luồng CI chạy.

## Cấu trúc

```
ex3-gradle-ci/
├── .github/
│   └── workflows/
│       └── ci.yml        # Workflow CI có cache: 'gradle'
├── gradle/wrapper/       # Gradle wrapper (gradlew, gradlew.bat, jar)
├── src/
│   ├── main/java/com/example/App.java
│   └── test/java/com/example/AppTest.java
├── build.gradle
├── settings.gradle
├── .gitignore
├── report.md             # So sánh thời gian trước/sau khi cache
└── README.md
```

## Điểm chính

Trong bước `actions/setup-java@v5` đã thêm tham số:

```yaml
- name: Thiết lập môi trường Java JDK 17
  uses: actions/setup-java@v5
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'gradle'
```

`cache: 'gradle'` khiến `setup-java` tự động:

- Tính khóa cache (cache key) dựa trên các tệp `**/*.gradle*`, `**/gradle-wrapper.properties`.
- Lần chạy đầu: chưa có cache → bước cài đặt tải toàn bộ dependency.
- Các lần chạy sau: khôi phục cache (`Cache restored successfully from key: gradle-...`)
  nên bước `./gradlew test` không phải tải lại thư viện.

## Chạy thử cục bộ

```bash
./gradlew test        # Linux/macOS
gradlew.bat test      # Windows
```

## Đẩy lên GitHub để kiểm chứng

```bash
git init
git add .
git commit -m "Bài 3: Gradle dependency caching"
git branch -M main
git remote add origin <URL-repo-cua-ban>
git push -u origin main
```

Sau đó sửa một file bất kỳ và push lần 2 để thấy cache được khôi phục.
Xem chi tiết kết quả trong [report.md](./report.md).
