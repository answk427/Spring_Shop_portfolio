# 🛒 Spring Shop Portfolio

Spring Boot를 이용한 **전자상거래 플랫폼**입니다.

---

## 📊 프로젝트 개요

| 항목 | 내용                        |
|------|---------------------------|
| **개발 기간** | 2026년 04월 ~ 2026년 05월     |
| **핵심 목표** | 단순 CRUD가 아닌 실무 수준의 시스템 설계 |

---

## 🔗 주요 링크

- **GitHub**: https://github.com/answk427/Spring_Shop_portfolio
- **배포 사이트**: https://jjsdev.duckdns.org
- **PPT 포트폴리오**: https://docs.google.com/presentation/d/1ZHQb8--vwEnniTyY3q30j5zhzwdCFpUCUzSq9Dzt-3s/edit?usp=sharing

---

## 시연 영상
### 판매 -> 구매 -> 장바구니 -> 주문
https://github.com/user-attachments/assets/324e111d-95ae-4fba-b9f7-1ccd0c8ca74b

### 주문 -> 주문확정 -> 배송완료
https://github.com/user-attachments/assets/567b9a0a-614c-4950-aa8d-f27a141c11b4

---
## 📚 기술 스택

| 분야 | 기술 |
|---|---|
| Backend | Spring Boot 3.5.5, Java 21 |
| Database | MySQL 8.0, Redis 7.0, AWS RDS |
| Authentication | JWT (JJWT), Spring Security |
| Query & ORM | JPA, QueryDSL |
| Mapping | MapStruct |
| Migration | Flyway |
| Testing | JUnit5, TestContainers |
| Infra | Docker, Docker Compose, Nginx, AWS EC2 |
| Storage & CDN | AWS S3, AWS CloudFront |
| DevOps | GitHub Actions |

---

### 아키텍처 다이어그램
<img width="1672" height="941" alt="Image" src="https://github.com/user-attachments/assets/dd631799-43f1-471f-831f-5fca0994a0a5" />

---
## ⭐ 핵심 기능

### 1️⃣ JWT 기반 인증/보안 🔐
```
✅ AccessToken (15분) + RefreshToken (7일) 조합으로 보안과 유지기간 연장
✅ Token Rotation: RefreshToken 사용 시 새로운 토큰 발급
✅ RefreshToken 만료: Redis TTL 기능으로 자동 만료
✅ AccessToken Blacklist: Redis 저장으로 즉시 로그아웃 보장
✅ AccessToken 만료시 /refresh API로 재요청, 재발급
```
<img width="1920" height="1080" alt="Image" src="https://github.com/user-attachments/assets/e69034fe-eace-46e7-a505-36a7bd7c8e4f" />

---

### 2️⃣ 주문 시스템 (부분 취소/반품 지원) 📦
```
Order와 OrderItem 분리:
- Order: 주문 컨테이너, OrderItem을 1:N관계로 가짐 
- OrderItem: 주문된 상품. 각 상품을 독립적으로 상태 관리(주문확정,취소,반품 등)

상태 흐름:
PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED / RETURNED
상태 변화의 일관성을 지키기 위해 변화 가능한 순서를 제약

특징:
✅ 1개 주문에서 일부 상품만 취소/반품 가능
✅ 상태 변화에 따른 구매자/판매자 입금/출금 연동
```
[코드 링크](https://github.com/answk427/Spring_Shop_portfolio/blob/640a99252657e637d9ce4f28ab87a3097c6af4f8/src/main/java/work/trade/order/domain/OrderItem.java#L70-L92)

**트러블 슈팅**
> 처음 설계는 Order(주문) 단위로 상태를 관리하는 것이었습니다.
> 
> 주문1(상품1, 상품2, 상품3)의 상태가 배송중, 배송완료, 취소, 반품 등 같이 관리되는 문제 발생
> 
> 개별 상품 별로 상태 관리가 필요. OrderItem에서 상태를 관리하도록 테이블 구조 변경
> 
> Flyway migration 파일을 이용해 버전업으로 깔끔한 테이블 구조 변경 완료
---

### 3️⃣ 동시성 제어 (Pessimistic Lock) 🔒
```
문제: 재고 10개인 상품에 100명이 동시에 구매 시도
      → 정확히 10명만 성공해야 함

해결책:
✅ Pessimistic Lock
✅ Product 조회 시 즉시 Lock 획득
✅ 다른 스레드는 Lock이 풀릴 때까지 대기

검증:
✅ 100개 스레드가 10개 재고 동시에 구매 시도 = 정확히 10명 성공 ✓

```
[코드 링크1](https://github.com/answk427/Spring_Shop_portfolio/blob/640a99252657e637d9ce4f28ab87a3097c6af4f8/src/main/java/work/trade/product/repository/ProductRepository.java#L24)

[코드 링크2](https://github.com/answk427/Spring_Shop_portfolio/blob/640a99252657e637d9ce4f28ab87a3097c6af4f8/src/test/java/work/trade/order/service/OrderServiceTest.java#L554-L616)

---

### 4️⃣ 쿼리 최적화 (N+1 해결) ⚡
N+1 문제 테스트 검출

<img width="584" height="530" alt="Image" src="https://github.com/user-attachments/assets/0c66317a-1065-4438-8679-2b953dc760c4" />

<img width="902" height="106" alt="Image" src="https://github.com/user-attachments/assets/e29438c1-c53e-4b5b-ae01-273f891c2e3b" />

```
Before (N+1 문제):
상품 500개 조회 → 501개 쿼리
(1개 메인 쿼리 + 500개 상품별 추가 쿼리)
Entity -> DTO 변환
(DTO 필드에 필요한 객체를 참조할때마다 추가 쿼리)

After (최적화):
FetchJoin → 1개 쿼리

기술:
✅ FetchJoin으로 관계 데이터 한 번에 로드
✅ QueryDSL로 DTO Projection 최적화
```
[코드 링크1](https://github.com/answk427/Spring_Shop_portfolio/blob/640a99252657e637d9ce4f28ab87a3097c6af4f8/src/main/java/work/trade/order/repository/order/OrderRepository.java#L29)

[코드 링크2](https://github.com/answk427/Spring_Shop_portfolio/blob/640a99252657e637d9ce4f28ab87a3097c6af4f8/src/main/java/work/trade/order/repository/order/OrderRepositoryCustomImpl.java#L22)

---

### 5️⃣ 동적 검색
```
2글자 이상의 단어를 키워드로 특정 카테고리에서 상품 검색

기술:
✅ QueryDSL로 복잡한 동적 조건 확장성 확보
✅ FullText Index로 검색 최적화
```
[코드 링크](https://github.com/answk427/Spring_Shop_portfolio/blob/640a99252657e637d9ce4f28ab87a3097c6af4f8/src/main/java/work/trade/product/repository/ProductRepositoryCustomImpl.java#L32
)

---
### 6️⃣ 파일 업로드 📁
```
설계 원칙: 로컬에서 개발 → 프로덕션에서 S3

FileUploadService (인터페이스)
├─ LocalFileUploadService (개발용)
└─ S3FileUploadService (프로덕션용)

전환 방법:
설정 1줄 변경만으로 로컬 ↔ S3 자동 전환
코드 수정 X

✅CloudFront로 캐싱, 접근 보안
✅DB에는 파일의 경로만 저장. HTTP요청 응답 반환시 FrontCloud의 도메인을 붙여서 URL 반환
   URL을 통째로 저장했을 시 발생하는 문제 방지 

---
```
[코드 링크](https://github.com/answk427/Spring_Shop_portfolio/blob/640a99252657e637d9ce4f28ab87a3097c6af4f8/src/main/java/work/trade/file/util/FileUrlResolver.java#L7)

---
### 7️⃣ 상품 이미지 관리 🖼️
```
구조:
✅ 썸네일: 상품 목록에 표시 (1개)
✅ 상세 이미지: 세로 상세 페이지 (여러 개)
✅ displayOrder로 순서 관리

성능 최적화:
✅ 목록 조회: LEFT JOIN + 썸네일만 가져오기

✅ 상세 조회: 한번에 모든 이미지를 가져오지 않고 스크롤 할때마다 필요한 이미지 Slice로 반환
```
[코드 링크](https://github.com/answk427/Spring_Shop_portfolio/blob/640a99252657e637d9ce4f28ab87a3097c6af4f8/src/main/java/work/trade/order/repository/orderItem/OrderItemRepositoryCustomImpl.java#L46-L55)

---

### 8️⃣ Flyway DB 버전관리 🖼️
✅DB 스키마 변경 이력 관리 자동화
✅팀원 간 로컬 DB 구조 불일치 방지하면서 구조 변경 용이

<img width="263" height="232" alt="Image" src="https://github.com/user-attachments/assets/2f521500-4db3-4c51-9955-f2e3e5951bcb" />

---

### 9️⃣ 개발 환경 구축 🖼️

<img width="1672" height="941" alt="Image" src="https://github.com/user-attachments/assets/25495a8d-31ee-4743-b9ed-bb36aa708e2f" />

<img width="1672" height="941" alt="Image" src="https://github.com/user-attachments/assets/0c8c5ada-b949-4a1f-90e3-1e588e300a4e" />

---

### 트러블슈팅
<img width="1672" height="941" alt="Image" src="https://github.com/user-attachments/assets/a7b5d260-217e-47b5-8f02-9f2dbeda62c4" />

---
### 프로젝트 후기
ㅇ 목표
- 단순 기능 구현을 넘어, N+1 문제와 동시성 문제 등 실제 서비스 환경에서 발생할 수 있는 문제를 고려하며 개발했습니다.
- 또한 협업 상황을 가정하여 도메인 중심 설계, 책임 분리, 낮은 결합도를 유지하려 노력했으며,
Flyway, Docker, GitHub Actions를 활용해 실제 운영과 유사한 개발 및 배포 환경을 구성했습니다.

ㅇ 배운점
- 기본적인 인증과 스프링 MVC로 이어지는 흐름을 이해하고 각 도메인 중심의 서비스로직 집중, DTO 활용 등 전반적인 백엔드에 대한 이해를 할 수 있었습니다.
- 테스트 코드의 중요성을 배웠습니다. 새로운 기능을 추가하거나 변경할때마다 기존 테스트코드가 잘 통과되는지 검사하는 과정에서 매우 유용하고 필수적이라고 느꼈습니다.

---

## 🚀 빠른 시작

### 1️⃣ 필수 준비물
```
Java 21+
Docker
```

### 2️⃣ 클론 및 설정(로컬 환경)
```bash
# 저장소 클론
git clone https://github.com/answk427/Spring_Shop_portfolio.git
cd Spring_Shop_portfolio

# docker-compose 컨테이너 실행
docker compose up -d

# 프로젝트 빌드
./gradlew build
```

```
배포환경은 AWS ACCESSKEY등의 보안 때문에 
private Repository에서 docker compose 파일 관리
```

---

## 📖 API 문서
[Swagger API 페이지](https://jjsdev.duckdns.org/swagger-ui/index.html)
### 인증
```
POST /api/auth/login        - 로그인
POST /api/auth/logout       - 로그아웃
POST /api/auth/refresh      - 토큰 갱신
POST /api/users             - 회원가입
```

### 상품
```
GET  /api/products                     - 전체 상품 조회 (페이징)
GET  /api/products/{id}                - 상품 상세
GET  /api/products/{id}/detail-images  - 상세 이미지 (무한 스크롤)
GET  /api/products/category/{id}       - 카테고리별 상품
GET  /api/products/search              - 상품 검색 (FULL TEXT)
GET  /api/products/my                  - 내 상품 목록 (판매자)

POST   /api/products                   - 상품 등록 (판매자)
PUT    /api/products/{id}              - 상품 수정 (판매자)
DELETE /api/products/{id}              - 상품 삭제 (판매자)

POST   /api/products/{id}/images       - 이미지 추가
DELETE /api/products/images/{id}       - 이미지 삭제
PATCH  /api/products/images/{id}/thumbnail - 썸네일 변경
```

### 장바구니
```
GET    /api/carts              - 내 장바구니 조회
POST   /api/carts              - 상품 추가
PUT    /api/carts/{id}         - 수량 변경
DELETE /api/carts/{id}         - 상품 삭제
DELETE /api/carts              - 장바구니 비우기
```

### 주문
```
POST   /api/orders                           - 주문 생성
GET    /api/orders                           - 내 주문 목록
GET    /api/orders/{id}                      - 주문 상세

PATCH  /api/orders/{id}/confirm              - 주문 확정 (결제)
PATCH  /api/orders/{id}/cancel               - 주문 취소

PATCH  /api/orders/items/{id}/ship           - 배송 시작 (판매자)
PATCH  /api/orders/items/{id}/deliver        - 배송 완료 (판매자)
PATCH  /api/orders/items/{id}/cancel         - 단건 취소
PATCH  /api/orders/items/{id}/return         - 반품
```

### 지갑 (결제/환불)
```
GET  /api/wallets              - 내 지갑 조회
GET  /api/wallets/records      - 거래 기록
```

---

## 🙏 감사합니다!
