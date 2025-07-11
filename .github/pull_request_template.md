### **커밋 링크**
<!-- 
좋은 피드백을 받기 위해 가장 중요한 것은 코드를 작성할 때 커밋을 작업 단위로 잘 쪼개는 것입니다.
모든 작업을 하나의 커밋에 진행하고 PR을 하면 구조 파악에 많은 시간을 소모하기 때문에 절대로
좋은 피드백을 받을 수 없습니다.


필수 양식)
커밋 이름 : 커밋 링크

예시)
동시성 처리 : c83845
동시성 테스트 코드 : d93ji3
-->

---
### **리뷰 포인트(질문)**

### 목적
- 정책정의
- 요구사항 기능 구현 (단순포인트조회, 히스토리조회, 충전, 사용 )
- 기능에 대한 테스트


### 정책
- 충전시 규칙 : 항상 양수로 충전 , 충전단위 5000원 ,최대 잔고 50만원
- 사용시 규칙 : 하루 2회 사용 , 한번에 최대 사용금액 (100000)
- 포인트 충전내역 조회 최신내역순으로 조회
- 포인트 히스토리는 얼마를 충전한지 사용한지 들어온 값자체를 저장된다.
- 충전실패,사용실패시에도 history 남기기 고려했지만 상태값 없어서 !!구현하지않음!!


### 1. 제가 생각한 방향과 맞게 테스트 코드를 작성했는지 질문드립니다.
처음에 스프링 컨테이너 없이 단위테스트를 진행하고싶어서 자바 자체에서 new로 실제 객체 사용해서 테스트를 진행했습니다.
하지만 이경우 너무 많은 의존성을  가지고있어서 프로덕트 코드가 바뀔때마다 주입을 계속 해줘야하는 문제가 있었습니다.
그렇다고 @mock을 사용하자니 어떤 메소드 몇번  호출했어? 이런 코드를 구현하면 블랙박스 테스트가 되지 않는단점이 있었습니다.

그래서 테스트구현시 무엇을 할지 기준을 정해봤습니다.
응답값 중심 테스트 -> 실제객체사용 (ex 생성시 잔액 0원인지)
호출여부/예외처리 mock (ex 잔액부족일떄 예외 메서드 호출하나)

어떤게 의미있는 테스트일지 고민을 했던것같습니다.. (단순호출테스트 구현하지않는대신 생성시 0원이 맞는지 이런식으로 변경)

## 의미있다고 생각한 테스트
- 히스토리 정렬(의미가있나..)
- 새로운 유저 생성시 0원이있는지
- 충전 , 사용 자체 통합테스트
- 사용시 예외확인
- 충전, 사용시 히스토리가 내역 잘 저장되는지 
- 도메인 테스트 (객체로)
- 컨트롤러에서 호출이 잘되는지 (원하는 응답형식으로 리턴되나)


### 2. 계층간의 역할의 이해가 부족한것같아요
컨트롤러 : 외부에서 받거나 사용자가 입력하는 데이터를 특정형식으로 받아서 서비스처리 후 특정형식으로 보여줄수있는 형식으로 변환하는 역할
         통신 잘되는지, 어떻게 들어왔고 어떻게나가는지 (api 형식이 변경되면 컨트롤러만 변경하게)
서비스 : 비지니스 로직 (서비스를 변경 단위의 기준으로 나눈다고 하셨는데 이해가 잘안갑니다ㅠ)
        도메인의 유효성이나 레파지토리의 null등을 그대로 boolean으로 받아서 예외처리나 기타 로직을 구현하는것
        서비스 그자체 라고 이해했습니다. 
레피지토리 : 디비 연결 등
도메인 : 특정 도메인이 스스로 알아야하는 정책등을 도메인에 구현 해서 해당 도메인의 규칙이 바뀐다면 도메인만 수정할수있도록 (잔고0원등)

###  3. 궁금증..
- jpa를 처음써봐서 Dto를 처음써봣는데 원래 이렇게 한개한개 응답할때마다 죄다 만들어야하는게맞을까요..?
- 프로젝션이나 더티체킹같은 개념도 공부하긴했는데 확실하게 하려면 dto를 주로 쓴다고 하긴하더라고요(구글검색기준)
  (인터페이스기반 프로젝션은 유지보수시에 어렵고 어차피이것도 파일을 만들어줘야하긴하더라고요..)
- 해야하는 api 호출이 많아질수록 다양한 응답과 다양한 리턴형태가 있을텐데 그때마다 무한정으로 만들어야하는지
- 아니면 합치는기준..? 이있는지 궁금합니다.. 
- (맞게쓴지도 모르겠습니다.. 현재는 리턴값자체가 똑같아도될것같아서 사용과 충전을 한번에 UserPointResponseDto로 묶었는데 혹시 사용과 충전의 반환이 달라질수도있으니까 확장성때문에 쪼개야하는지..? )
- 그리고 서비스에서 대체적인 기능 테스트를 다해서 컨트롤ㄹ러에서는 응답값과 리턴정도만 확인헀는데
- 여기서도 똑같이 테스트를 해야할까요? 중복인것 같아서..구현하진않앗습니다.
<!-- - 리뷰어가 특히 확인해야 할 부분이나 신경 써야 할 코드가 있다면 명확히 작성해주세요.(최대 2개)
  
  좋은 예:
  - `ErrorMessage` 컴포넌트의 상태 업데이트 로직이 적절한지 검토 부탁드립니다.
  - 추가한 유닛 테스트(`LoginError.test.js`)의 테스트 케이스가 충분한지 확인 부탁드립니다.

  나쁜 예:
  - 개선사항을 알려주세요.
  - 코드 전반적으로 봐주세요.
  - 뭘 질문할지 모르겠어요. -->
---
### **이번주 KPT 회고**

### Keep
<!-- 유지해야 할 좋은 점 -->
퇴근하구 공부함 ~
테스트코드를 거의 구현해보지않았는데 다양하게 수정을 하면서 테스트 목적을 생각하다보니 
냅다 코드를 작성할때보다는 꼼꼼한 분석이 가능해졌다.

### Problem
<!--개선이 필요한 점-->
어떤 테스트가 중요한지(전부테스트를 하진않는다) 어떻게 테스트를 구현(MOCK/실제객체)하는지에 대한 방법을 좀더 고민해봐야한다.
요구사항 분석에 대한 깊이를 생각해봐야겠다.
테스트 코드를 구현하다보니 계층간의 책임에 대해 생각을 많이했다 평소에는 여기저기 넣었던거같은데 
각각의 책임을 생각하면서 구현을 하려고 했다 


### Try
<!-- 새롭게 시도할 점 -->
동시성 구현하기 ! 
