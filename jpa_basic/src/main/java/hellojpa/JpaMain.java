package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
//            // 엔티티를 생성한 상태 (비영속)
//            Member member = new Member();
//            member.setId(100L);
//            member.setName("회원1");
//
//            // 영속 (아직 DB에 저장되지 않음)
//            System.out.println("=== BEFORE ===");
//            // 1차 캐시에 저장됨
//            em.persist(member);
//            System.out.println("=== AFTER ===");

            // DB가 아닌 1차 캐시에서 가져옴
            // DB에 있는 경우 select문으로 1차 캐시로 가져옴
            // findMember1에서 select문으로 DB에서 가져오고, findMember2에서는 1차 캐시에서 가져온다.
            Member findMember1 = em.find(Member.class, 150L);
            Member findMember2 = em.find(Member.class, 150L);


            System.out.println("findMember.getId() = " + findMember1.getId());
            System.out.println("findMember.getName() = " + findMember1.getName());
            Member member1 = new Member(150L, "memberA");
            Member member2 = new Member(160L, "memberB");

            em.persist(member1);
            em.persist(member2);

//            System.out.println("======================");
//
//            // 커밋하는 순간 DB에 INSERT SQL을 보낸다.
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
