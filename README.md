# 🚁 AeroChain: 드론 멀티배송 시스템
<br/>

> 💻 개발 기간 | 2025.11.06 ~ 2025.12.12

<p align="center">
  <img width="822" alt="aerochain-main-image" src="https://github.com/user-attachments/assets/a1d31be1-436e-4097-ad13-34ba55dbe364" />
</p>

---
<br/>

## 💎 서비스 소개

> **경로 최적화와 실시간 추적을 통해 배송의 미래를 제시합니다.**

**AeroChain**은 이커머스 시장의 급증하는 근거리 배송 수요에 대응하기 위한 **드론 멀티 배송 시스템**입니다. 기존 운송 수단의 환경 오염, 인력 부족, 도심 교통 혼잡 문제를 해결하고, 여러 주문을 하나의 비행 임무로 최적화하여 효율성을 극대화합니다.

<br/>

<p align="center">
  <img width="822" alt="Drone Delivery Process" src="https://github.com/user-attachments/assets/72d9bbe2-fd99-40cf-bdbc-c1027df71606" />
  <br/>
  <strong>기존의 편의점 어플리케이션과 유사한 UI를 통해 편하게 주문이 가능합니다.</strong>
  <br>
  <small>고객은 주변의 매장에서 재고를 확인할 수 있으며, 쉽게 주문 및 결제가 가능합니다.</small>
</p>
<br/>

<p align="center">
  <img width="822" alt="Route Optimization" src="https://github.com/user-attachments/assets/ec3f9bfa-cbf2-407d-98cb-e8106a321cb4" />
  <br/>
  <strong>주문부터 배송까지, 모든 과정을 실시간으로 추적하고 관리할 수 있습니다.</strong>
  <br>
  <small>고객은 자신의 주문 현황을, 점주는 전체 배송 플로우를 한눈에 파악합니다.</small>
</p>
<br/>

## 👨‍💻 팀원 소개

<br/>

<table align="center">
  <tr>
    <td align="center" width="25%">
      <img width="460" height="460" alt="Image" src="https://github.com/user-attachments/assets/057399e3-32c3-43b1-8104-c9d1ee0e694e" /><br/>
      <b>정원준</b><br/>
      <code>Backend Developer</code><br/>
      <a href="https://github.com/dietken1">GitHub</a>
    </td>
    <td align="center" width="25%">
      <img width="460" height="460" alt="member2" src="https://github.com/user-attachments/assets/6f34a120-5896-4438-9c4f-82d18736f33e" /><br/>
      <b>안재관</b><br/>
      <code>Frontend Developer</code><br/>
      <a href="https://github.com/jaegwanan">GitHub</a>
    </td>
    <td align="center" width="25%">
      <img width="460" height="460" alt="member3" src="https://github.com/user-attachments/assets/fc48717c-fd1b-4d4d-8202-92a774b4ea5a" /><br/>
      <b>최미현</b><br/>
      <code>Frontend Developer</code><br/>
      <a href="https://github.com/chlalgus">GitHub</a>
    </td>
    <td align="center" width="25%">
      <img width="460" height="460" alt="member4" src="https://github.com/user-attachments/assets/70a7577f-ba87-4701-b574-b170450b3de6" /><br/>
      <b>나희원</b><br/>
      <code>Designer</code><br/>
      <a href="https://github.com">GitHub</a>
    </td>
  </tr>
</table>
<br/>

## ⚙️ 기술 스택
| 👀 Category | ⚒️ Tech | 📝 Description |
|:---:|:---:|:---|
| **`Backend`** | Java 17, Spring Boot | 백엔드 프레임워크 및 데이터베이스 연동 |
| **`Frontend`** | React, Kakao Map API, SockJS | UI, 지도 시각화 및 WebSocket 클라이언트 |
| **`Database`** | MySQL | 관계형 데이터베이스 |
| **`Real-time`** | WebSocket (STOMP) | 실시간 양방향 통신 |
| **`DevOps`** | Docker,  GitHub Actions, AWS EC2, Nginx | 컨테이너화, CI/CD 및 배포 환경 |
| **`Collaboration`** | GitHub, Swagger, Notion, Figma, Postman | 협업, API 문서화 및 디자인 |

<br/>

## 📊 ERD

> 데이터베이스 설계는 주문, 배송, 로그 추적에 중점을 두었으며, 과거 배송 경로 재현을 위해 의도적인 반정규화를 적용했습니다.

<p align="center">
<img width="1000" alt="erd" src="https://github.com/user-attachments/assets/626a465e-69df-4b3b-bcdd-22b6fa2816ca" />
</p>
<br/>

## 🌳 백엔드 패키지 구조
```
backend.databaseproject
├── domain
│   ├── drone
│   │   ├── entity
│   │   ├── exception
│   │   └── repository
│   ├── order
│   │   ├── controller
│   │   ├── dto
│   │   ├── entity
│   │   ├── exception
│   │   ├── repository
│   │   └── service
│   ├── product
│   │   ├── entity
│   │   └── repository
│   ├── route
│   │   ├── controller
│   │   ├── dto
│   │   ├── entity
│   │   ├── repository
│   │   ├── scheduler
│   │   └── service
│   ├── store
│   │   ├── controller
│   │   ├── dto
│   │   ├── entity
│   │   ├── repository
│   │   └── service
│   └── user
│       ├── controller
│       ├── dto
│       ├── entity
│       ├── repository
│       └── service
└── global
    ├── common
    ├── config
    ├── exception
    └── handler
```
