# nestedCommentService

*개발할 때 사고의 흐름등을 주석으로 자주 기록해놓는 편인데, 첫 깃허브사용이라 주석 삭제 및 사소한 수정으로 인해 쓸데없이 변경내용이 상당히 많이 잡혔다. 다음부턴 개선하도록 하자



# 사용기술
spring boot 3.0.6 - mvc방식 위주 개발


thymeleaf3


Hibernate ORM core version 6.1.7


mysql 8.0.33


spring-security-web-6.0.3



# 핵심 기술 구현

## 대댓글의 CASCDING기능 - 부모댓글이 삭제될 시 연계삭제 여부를 결정하는 변수 사용

	엔티티의 Boolean isAffected 필드를 통해 구현하였다.
	대댓글들만 이 값을 설정하고, 부모댓글이 없는 루트댓글들은 이 값을 설정하지 않는다(null로 설정한다)
	값이 True면 부모댓글이 삭제될 시 대댓글도 자동으로 삭제된다.
	값이 False면 부모댓글이 삭제되더라도 대댓글은 자동으로 남아있다. 
댓글 수정 시에도 댓글 작성자가 해당 옵션을 변경할 수 있게 허용했다.


## 대댓글 작성

-하나의 API를 통해 루트댓글저장과 대댓글저장을 한번에 처리


## 대댓글 출력구조
## 대댓글 출력구조-뷰(타임리프)
## 대댓글 출력구조-데이터는 어떻게 전송했는가
organizechilds
+기존구조의 문제점

## jpa
## springsecurity

## 수정기능구현:javascript fetch, await

## 타임리프
