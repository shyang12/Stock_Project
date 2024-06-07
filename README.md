# Stock_Project
## ▶ 네이버 주식 정보를 기반으로 모의 투자 서비스 구현
 
 - Eclipse에서 Java를 기반으로 모의 주식 투자 서비스를 구현하는 프로젝트

`JSOUP` `SWING` `Parsing` `Thread`

## 1. Co-Development Environment   
### 1. 1 Environments
- Windows 10
- Java
- Eclipse

### 1. 2 Skills
- Jsoup
- Swing

### 1. 3 Implement
- KOSPI top 100의 정보를 볼 수 있는 기능 -> `가격이 올라간 주식은 빨간색, 내려간 주식은 파란색 색깔 적용` + `Parsing`
- KOSDAQ 주식의 정보를 볼 수 있는 기능 -> `가격이 올라간 주식은 빨간색, 내려간 주식은 파란색 색깔 적용` + `Parsing`
- KOSPI, KOSDAQ의 현제 주식 현황을 확인하는 기능 -> `가격이 올라간 주식은 빨간색, 내려간 주식은 파란색 색깔 적용` + `Parsing`
- 초마다 주식의 순위, 가격등 변하는 값들은 1초마다 갱신되도록 적용 -> `Thread`를 사용하여 초마다 파싱한 값을 갱신
- 주식을 모의로 매수, 매도하는 기능
- 보유한 주식 현황을 보여줌
  
## 2. Result   
- KOSPI top 100
  
![주식 1](https://github.com/shyang12/Stock_Project/assets/85710913/43a7616f-e0fa-408a-a0bf-83a3f2a516c4)


- 주식 현황 페이지
  
![주식 2](https://github.com/shyang12/Stock_Project/assets/85710913/a74d2138-a2c8-485e-8812-6c47cec96291)


- 내가 매수한 주식

![주식 3](https://github.com/shyang12/Stock_Project/assets/85710913/2a16f7f9-dac0-4939-90f9-1f6a8ae402c9)

