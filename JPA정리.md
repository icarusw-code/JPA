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

- ```java
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

## 플러시



















