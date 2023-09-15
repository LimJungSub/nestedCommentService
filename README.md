# nestedCommentService

*사실 댓글이라기 보다는 토막글 개념으로 간단히 소통히 가능한 게시판기능이다.

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

대댓글들만 이 값을 설정하고, 부모댓글이 없는 루트댓글들은 이 값을 설정하지 않는다.(null로 설정한다)

값이 True면 부모댓글이 삭제될 시 대댓글도 자동으로 삭제된다.

값이 False면 부모댓글이 삭제되더라도 대댓글은 자동으로 남아있다.

댓글 수정 시에도 댓글 작성자가 해당 옵션을 변경할 수 있게 허용했다.
 ![image](https://github.com/LimJungSub/nestedCommentService/assets/80201699/02b210ac-eca2-4189-b981-ce13e4770ab5)


## 무한대댓글 출력구조

### 출력구조

이중루프를 통해 구현하였다.

우선 1차적으로 루트댓글들을 순회하며 출력하고, 해당 댓글에 자식댓글이 있다면 또 순회하며 자식댓글을 출력한다.

자식댓글을 출력하며 마지막에 한번 더 자식댓글에 자식댓글이 있는지 검사하고,

있다면 Thymeleaf의 fragment와 replace기능으로 재귀호출을 통해 구현하였다. 

전체적인 구조는 아래와 같다.

```html
*** index.html

<div th:if="!${commentList.isEmpty()}" th:each="comment:${commentList}" class="d-flex mb-1">
                    ...
                    <div th:if="!${comment.childComments().isEmpty()}">
                        <th:block th:replace="::grandComment(${comment.childComments()})"></th:block>
                    </div>
                    ...
                     <th:block th:fragment="grandComment(childComments)">
                     ...
                            <div th:if="${childComment.childComments() != null}">
                                <div th:if="!${childComment.childComments().isEmpty()}">
                                    <th:block
                                            th:replace="::grandComment(${childComment.childComments()})"></th:block>
                                </div>
                            </div>
                     </th:block>
</div>
```

### 정렬

대댓글이냐 댓글이냐에 따라 정렬기준을 다르게 적용했다.

우선 컨트롤러에서 뷰쪽으로 넘겨주는 commentList에는 루트댓글들만 넘겨준다. 루트댓글들만 넘겨주어도 계층형식으로 대댓글들이 연결되어있기때문에 괜찮다.

컨트롤러에서 @PageableDefault 어노테이션을 사용하여 루트댓글들을 정렬된 상태로 가져왔다.


```java
*** CommentService.java

    public Page<CommentDto> getRootComments(Pageable pageable) {
        List<Comment> list = commentRepository.findByParentIdIsNull();
        return commentRepository.findByParentIdIsNull(pageable).map(CommentDto::toDto);
```

```java
***CommentController.java

@GetMapping("/")
    public String comments(
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap modelMap
    )
    {
        //서비스에서 다 dto로 바꿔서 컨트롤러에게 줌Root
        Page<CommentDto> list = commentService.getRootComments(pageable);
        List<Integer> paginationBar = paginationBarService.returnNavList(pageable.getPageNumber(), pageable.getPageSize());
        modelMap.addAttribute("paginationBar",paginationBar);
        modelMap.addAttribute("commentList",list);
        //대댓글은 정렬방법이 다르기때문에 별도의 대댓글들을 컨트롤러계층이전에서 정렬하여 내려준다.
        return "/index";
    }
```

대댓글은 정렬방법이 오름차순이다. 컨트롤러계층이전에서 대댓글들을 오름차순으로 정렬하여 루트댓글의 Set<CommentDto> childComments에 저장하였다.




### 대댓글 출력구조-데이터는 어떻게 전송했는가
organizechilds
+기존구조의 문제점


## 대댓글 작성

-하나의 API를 통해 루트댓글저장과 대댓글저장을 한번에 처리




## jpa
## springsecurity

## 수정기능구현:javascript fetch, await

## 타임리프
