[TOC]



# JPA(Java Persistence API)

## SQL 중심 개발의 문제점

- 무한반복, 지루한 코드가 된다.
- **SQL**에 **의존적인 개발**을 피하기 어렵다. 
- 객체지향프로그래밍 vs 관계형 DB의 패러다임 불일치
- 객체의 저장은 어쩔 수 없이 관계형DB에 이루어진다.
- 상속관계, 연관관계, 

## JPA  개념

- 자바 진영의 ORM 기술 표준

**ORM(Object-relational mapping) - 객체 관계 매핑**

- 객체는 객체대로 설계
- 관계형 데이터베이스는 관계형 데이터 베이스대로 설계
- ORM 프레임워크가 중간에서 매핑

**JAVA 애플리케이션 -> JPA -> JDBC API  <=> DB (패러다임의 불일치 해결)**

**JPA는 표준 명세**

- 인터페이스의 모음
- 하이버네이트: 구현체

**JPA의 필요성**

- 생산성이 좋다.
- 유지 보수에 용이 (기존: 필드 변경시 모든 SQL 수정)
- 패러다임의 불일치 해결

**JPA 구동 방식**

![image](https://user-images.githubusercontent.com/77170611/150338499-b03d4cdf-055f-4e1c-aa06-620ad0b3774f.png)

## 영속성 관리

### **EntityMangerFactory & EntityManger 구조**

![image](https://user-images.githubusercontent.com/77170611/150339015-a9a37671-4ec7-493b-b508-8806e8901f87.png)

**EntityMangerFactory**

- 데이터베이스 하나당 어플리케이션은 일반적으로 하나의 EntityMangerFactory를 생성한다.
- 한 개의 EntityMangerFactory로 어플리케이션 전체에 공유하도록 설계한다.
- 여러 스레드가 동시에 접근해도 안전, 서로 다른 스레드 간 공유가 가능하다.

**EntityManger**

- 여러 스레드가 동시에 접근하면 동시성 문제가 발생한다.
- **스레드간의 공유는 절대 하면 안된다!!**
- 데이터베이스 연결이 필요한 시점까지 커넥션을 얻지 않는다.

### **영속성 컨텍스트**

- 엔티티를 영구 저장하는 환경

- 엔티티를 식별자 값(@id로 테이블의 기본 키와 매핑한 값)으로 구분한다.

  ```java
  @Entity
  public class Member{
  	@Id
  	private Long id;
  	private String name;
  }
  ```

- ```java
  // 엔티티 영속
  EntitiyManger.persist(entity);
  ```

- 엔티티 매니저를 사용해서 회원 엔티티를 영속성 컨텍스트에 저장

- **엔티티 매니저를 통해서 영속성 컨텍스트에 접근하고 관리** 할 수 있다.

- 엔티티 매니저를 생성할 때 하나 만들어진다.

### **엔티티의 생명주기**

- 비영속(new/transient) : 영속성 컨텍스트와 전혀 관계가 없는 상태
- 영속(managed): 영속성 컨텍스트에 **관리**되는 상태
- 준영속(detached): 영속성 컨텍스트에 저장되었다가 **분리**된 상태
- 삭제(removed): **삭제**된 상태

![image](https://user-images.githubusercontent.com/77170611/150340890-58186d00-c83d-4845-8cf5-1f82b32f9620.png)

- **비영속**

![image](https://user-images.githubusercontent.com/77170611/150341155-6f95cec0-f24a-4665-9879-61de2015cd74.png)

```java
// 객체를 생성한 상태(비영속)
Member member = new Member();
member.setId(100L);
member.setName("회원1");
```

- **영속**

![image](https://user-images.githubusercontent.com/77170611/150341536-59150626-2d57-4ac1-ad35-e649c51d1d4b.png)

```java
// 객체 생성
Member member = new Member();
member.setId(100L);
member.setName("회원1");

EntitiyManager em = emf.createEntitiyManager();
EntitiyTransaction tx = em.getTransaction();
// 트렌스젝션 시작
tx.begin();
// 객체를 저장한 상태(영속)
System.out.println("=== BEFORE ===");
// 1차 캐시에 저장됨
em.persist(member);
System.out.println("=== AFTER ===");
// 커밋하는 순간 DB에 INSERT SQL를 보낸다.
tx.commit();
```

![image](https://user-images.githubusercontent.com/77170611/150343216-84e635f6-97e3-4a52-9738-2323201ed16c.png)

**결과** 

- em.persist(member) 당시에는 아직DB에 저장되지 않은 상태이다. (1차 캐시에 저장됨)
- tx.commit() 이 이루어지는 순간에 DB에 INSERT** SQL를 보낸다.

### 영속성 컨텍스트의 이점

- 1차 캐시
- 동일성 보장
- 트랜잭션을 지원하는 쓰기 지연
- 변경 감지(Dirty Checking)
- 지연 로딩

**데이터베이스 조회과정**

```java
Member findMember1 = em.find(Member.class, 150L);
Member findMember2 = em.find(Member.class, 150L);
```

![image](https://user-images.githubusercontent.com/77170611/150348172-e5462543-d961-47ff-a2d6-e8bb04d981bb.png)

- 처음 조회할때 DB에서 가져오는것이 아닌 **1차캐시**에서 가져온다.
- 1차캐시에 없고, DB에 있는 경우 **DB에서 select문으로 1차 캐시**로 가져온다.
- findMember1에서 select문으로DB에서 가져오고, findMember2에서는 1차 캐시에서 가져온다.

**실행결과**

![image](https://user-images.githubusercontent.com/77170611/150348044-6bfe9dd6-b228-4eca-8abc-4272d12babaa.png)

**트랜잭션을 지원하는 쓰기 지연**

```java
EntitiyManager em = emf.createEntityManager();
EntityTransaction tx = em.getTransaction();
// 엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야 한다.
tx.begin();

// 쓰기지연 SQL저장소에 INSERT문이 저장된다.
em.persist(memberA);
em.persist(memberB);

// 트랜잭션 커밋이 이루어질때 INSERT SQL를 DB에 보낸다.
tx.commit();
```

![image](https://user-images.githubusercontent.com/77170611/150352804-c5b0972e-f4ba-4840-be37-8876be25d9f5.png)

**엔티티 수정- 변경감지(Dirty Checking)**

```java
EntitiyManager em = emf.createEntityManager();
EntityTransaction tx = em.getTransaction();
tx.begin();

Member memberA = em.find(Member.class, "memberA");

// update문을 따로 작성하지 않아도 된다.
memberA.setUsername("memberAAA");
memberA.setAge(10);

tx.commit();
```

![image](https://user-images.githubusercontent.com/77170611/150353804-e961ee4f-b25c-48fc-8790-95ac0f9fa03b.png)

**수정순서**

1. 트랜잭션 커밋 -> 엔티티 매니저 내부에서 플러시 호출
2. 엔티티와 스냅샷을 비교해서 변경된 엔티티를 찾는다.
3. 변경된 엔티티가 있으면 수정 쿼리를 생성해서 쓰기 지연 SQL저장소로 보낸다.
4. 쓰기 지연 저장소의 SQL을 DB로 보낸다.
5. 데이터베이스 트랜잭션을 커밋한다.

### 플러시

- 영속성 컨텍스트의 변경내용을 데이터베이스에 반영한다.
- 플러시 실행 메커니즘
  1. 변경 감지 동작(모든 엔티티를 스냅샷과 비교)
  2. 수정된 엔티티는 수정쿼리를 만들어 **쓰기 지연 SQL저장소** 등록
  3. 쓰기 지연 SQL 저장소의 쿼리를 DB에 전송
- **플러시를 한다고 1차 캐시가 사라지는 것은 아니다.(영속성 컨텍스트에 보관된 엔티티는 유지된다.) **
- **변경된 내용을 DB에 적용하는 것 뿐이다.**

**플러시하는 방법**

1. em.flush() 직접 호출
2. 트랙잭션 커밋시 플러시가 자동 호출
3. JPQL 쿼리 실행시 플러시가 자동 호출

- 직접 호출은 자주 사용하지는 않는다.

### 준영속 상태

- 영속상태 -> 준영속 상태
- 영속 상태의 엔티티가 영속성 컨텍스트에서 분리(detached)
- 영속성 컨텍스트가 제공하는 기능을 사용 못함

### 트랜잭션 범위의 영속성 컨텍스트

- @Transaction 어노테이션 

  ![image](https://user-images.githubusercontent.com/77170611/150523895-f1c97065-5ec8-4559-ac49-db90259c7bf7.png)

- 트랜잭션이 같으면 같은 영속성 컨텍스트 사용한다.

  ![image](https://user-images.githubusercontent.com/77170611/150524091-a4decf43-2d30-482f-a715-5ca38e5410d0.png)

- 트랜잭션이 다르면 다른 영속성 컨텍스트를 사용한다.

  ![image](https://user-images.githubusercontent.com/77170611/150524107-f4fde9eb-dc30-4ea7-b97f-bb820d437191.png)

## 엔티티 매핑

### 엔티티 매핑 소개

|        매핑        |       어노테이션        |
| :----------------: | :---------------------: |
| 객체와 테이블 매핑 |     @Entity, @Table     |
|    기본 키 매핑    |           @Id           |
|  필드와 컬럼 매핑  |         @Column         |
|   연관관계 매핑    | @ManyToOne, @JoinColumn |

### @Entity

- @Entity가 붙은 **클래스**는 JPA가 관리, **엔티티라고 한다.**

- JPA를 사용해서 테이블과 매핑할 클래스는 **@Entity**가 필수이다.

- 기본 생성자를 필수로 작성해주자(파라미터가 없는 public 또는 protected 생성자)

- final클래스, enum, interface, inner 클래스에는 사용할 수 없다.

- 저장할 필드에 final을 사용하면 안된다.

  ```java
  @Entity
  public class Member{
      private Long id;
      private String name;
  }
  
  // 기본 생성자를 생성해준다.
  public Member(){}
  
  // 사용할 생성자 
  public Member(Long id, String name){
      this.id = id;
      this.name = name;
  }
  ```

### @Table

- 엔티티와 매핑할 테이블을 지정한다.

  ```java
  @Entity
  @Table(name="MEMBER")
  public class Member{
      ...
  }
  ```

### 데이터베이스 스키마 자동 생성

- DDL(Date Definition Language)을 애플리케이션 실행 시점에 자동 생성

- |    옵션     |                      설명                       |
    | :---------: | :---------------------------------------------: |
    |   create    |   기존테이블 삭제 후 다시 생성(DROP + CREATE)   |
    | create-drop |     create와 같으나 종료시점에 테이블 DROP      |
    |   update    | 변경분만 반영(운영DB에는 절대 사용하면 안된다.) |
    |  validate   |    엔티티와 테이블이 정상 매핑되었는지 확인     |
    |    none     |                  사용하지 않음                  |

- ```java
  <property name="hibernate.hbm2ddl.auto" value="create" />
  ```

- **validate 또는 none만 사용할 것을 권장한다!!**

### 다양한 매핑 사용

```java
@Entity 
public class Member { 
 
    @Id // Pk로 매핑
    private Long id; 

    // 컬럼 매핑
    @Column(name = "name") 
    private String username; 
    
    private Integer age; 

    // 자바 enum 타입을 매핑할 떄 사용
    @Enumerated(EnumType.STRING) 
    private RoleType roleType; 

    // 날짜 타입을 매핑할 때 사용
    @Temporal(TemporalType.TIMESTAMP) 
    private Date createdDate; 

    @Temporal(TemporalType.TIMESTAMP) 
    private Date lastModifiedDate; 

    @Lob 
    private String description; 
    
    // 주로 메모리상에만 임시로 어떤 값을 저장할 때 사용 ---> DB에 반영 되지 않는다.
    @Transient
    private int tmp;
}
```

**@Enumerate**

- 기본값이 ORDINAL이다 : enum 순서를 데이터베이스에 저장한다.(0, 1, ,2 ...)
- **ORDINAL값으로 저장하게 되면 변경이 있을 때 그 전 데이터는 변하지 않기 때문에 큰 문제를 야기할 수 있다!!!**
- 거의 무조건 **EnumType.STRING (: enum 이름을 데이터베이스에 저장)**을 사용

### 기본 키 매핑

- **@Id**
- **@GeneratedValue(기본값: 자동생성)**

```java
public class Member{

	@Id @GeneratedValue
	private Long id;
}
```

### 데이터 중심 설계의 문제점

- 객체 설계를 테이블 설계에 맞추게 되면 **테이블의 외래키를 객체에 그대로 가져오게 된다.**
- 객체 그래프 탐색이 불가능 하다.
- 참조가 없으므로 UML(통합모델링언어) 도 잘못되었다.

```java
// order를 주문한 멤버를 찾고자 할때  ---> 객체지향적이지 않다.
Order order = em.find(Order.class, 1L);
Long memberId = order.getMemberId();

Member member = em.find(Member.class, memebrId);
```

## 연관관계 매핑 

- 방향 : 단방향, 양방향
- 다중성: 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M)
- 연관관계의 주인(Owner): 객체를 양방향 연관관계로 만들면 **연관관계의 주인을 정해야 한다.**



### 단방향 연관관계

**연관관계가 없는 객체인 경우** -> 외래 키 식별자를 직접 다룬다.

![image](https://user-images.githubusercontent.com/77170611/150561529-8f75c016-0974-4900-8233-5f76d9cea5c9.png)



**객체 연관관계를 사용한 경우**

![image](https://user-images.githubusercontent.com/77170611/150562260-14fec799-2b66-4d8f-a4f4-f5ccd7b5860d.png)

```java
// Member 
@ManyToOne // 멤버 입장에서 봐야한다. 멤버가 N이고 One인 Team으로 매핑한다.
@JoinColumn(name = "TEAM_ID")
private Team team;
```

```java
// 팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

// 회원 저장
Member member = new Member();
member.setUsername("member1");
// JPA가 알아서 team에서 Pk값을 꺼내서 INSERT할때 FK으로 사용한다.
member.setTeam(team);
em.persist(member);
```

- @ManyToOne
  - 다대일(N:1) 관계라는 매핑 정보
  - 어노테이션 필수
- @JoinColumn(name="TEAM_ID")
  - 외래키(FK)를 매핑할 때 사용
  - name 속성에 매핑할 외래 키 이름을 지정한다.
  - 생략 가능하다.























