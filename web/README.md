# rs-grid-service Auth Test (Node.js)

`rs-grid-service` backend'inin `auth-controller` (login / refresh / logout) ve admin-only
kullanıcı oluşturma API'lerini test etmek için bağımlılıksız (dependency'siz) bir Node.js
static server + tek dosyalık web arayüzü.

## Çalıştırma

```bash
npm start
```

veya doğrudan:

```bash
node server.js
```

Sonra tarayıcıda **http://localhost:5500** adresini açın.

Farklı bir port kullanmak isterseniz:

```bash
PORT=3000 node server.js
```

## Backend ayarı

`index.html` içinde en üstte sabit tanımlı API adresini bulabilirsiniz:

```js
const API_BASE_URL = "http://localhost:8081";
```

`rs-grid-service` backend'iniz farklı bir adres/portta çalışıyorsa bu satırı güncelleyin.

## Özellikler

- Login / Refresh / Logout (`/api/v1/auth/**`)
- Access token süresi dolmadan ~30 saniye önce otomatik yenileme
- Token'dan (JWT decode ile) yetkiler (realm rolleri) ve grup bilgisi gösterimi
- `admin` rolüne sahip kullanıcılar için sol menüde **Kullanıcı İşlemleri**:
  - `/api/v1/groups`'tan beslenen "Kullanıcı Tipi" dropdown'ı ile yeni kullanıcı oluşturma
  - `/api/v1/users` ile kullanıcı listeleme (arama dahil)
  - Kullanıcı düzenleme (ad, soyad, email, aktif/pasif, şifre sıfırlama) ve silme
  - Not: kullanıcı tipi (userType) şu an sadece oluşturma sırasında atanabiliyor;
    `PUT /api/v1/users/{id}` backend'de bu alanı desteklemediği için düzenleme
    ekranında salt-okunur gösteriliyor
