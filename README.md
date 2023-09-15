# nestedCommentService

*사실 댓글이라기 보다는 토막글 개념으로 간단히 소통히 가능한 게시판기능이다.

*개발할 때 사고의 흐름등을 주석으로 자주 기록해놓는 편인데, 첫 깃허브사용이라 주석 삭제 및 사소한 수정으로 인해 쓸데없이 변경내용이 상당히 많이 잡혔다. 다음부턴 개선하도록 하자



# 사용기술
spring boot 3.0.6 - mvc방식 위주 개발


thymeleaf3


Hibernate ORM core version 6.1.7


mysql 8.0.33


spring-security-web-6.0.3

javascript fetch api


# 핵심 기술 구현

## 대댓글의 CASCDING기능 - 부모댓글이 삭제될 시 연계삭제 여부를 결정하는 변수 사용

엔티티의 Boolean isAffected 필드를 통해 구현하였다.

대댓글들만 이 값을 설정하고, 부모댓글이 없는 루트댓글들은 이 값을 설정하지 않는다.(null로 설정한다)

값이 True면 부모댓글이 삭제될 시 대댓글도 자동으로 삭제된다.

값이 False면 부모댓글이 삭제되더라도 대댓글은 자동으로 남아있다.

대댓글 수정 시에도 대댓글 작성자가 해당 옵션을 변경할 수 있게 허용했다.
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

우선 컨트롤러에서 뷰쪽으로 넘겨주는 commentList에는 루트댓글들만 넘겨준다. 루트댓글들만 넘겨주어도 루트댓글이 자식댓글을 가지고있기 때문에 괜찮다.

컨트롤러에서 @PageableDefault 어노테이션을 사용하여 **루트댓글들**을 정렬된 상태로 가져왔다.


```java
*** CommentService.java

    public Page<CommentDto> getRootComments(Pageable pageable) {
        List<Comment> list = commentRepository.findByParentIdIsNull();
        return commentRepository.findByParentIdIsNull(pageable).map(CommentDto::toDto);
   }
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

**대댓글들**은 정렬방법이 오름차순이다. 컨트롤러계층이전에서 대댓글들을 오름차순으로 정렬하여 루트댓글의 Set<CommentDto> childComments에 저장하였다.

서비스계층에서 루트댓글들에 .toDto()를 통해 Dto형식으로 변환할 때, 자식댓글을 오름차순 정렬하여 갖고있게 하며,

자식댓글의 자식댓글들 (즉 N차대댓글들)도 정렬하여 갖고 있게한다.

여기서 중요한점은 화면 출력 시 정렬은 db에 저장되어있는 객체들의 정렬방식을 따르지 않기때문에, 별도로 이렇게 한번 더 DTO차원에서 정렬을 해준 것이다. (개발하며 이 부분을 몰라 상당히 많은 시간이 소요됐었다)

```java
***CommentDto.java (record)

public static CommentDto toDto(Comment comment){
        //대댓글정렬
        Comparator<CommentDto> comparator = Comparator.comparing(CommentDto::createdDate).thenComparingLong(CommentDto::id);

        //직계자식을 담은 Set 설정
        Set<CommentDto> childSet = comment.getChildComments().stream().map(
                        c-> { 
                            //대댓글의 댓글(N차대댓글)들도 대댓글과 정렬로직이 똑같기떄문에(등록순), 똑같은 comparator 적용
                            Set<CommentDto> tmpSet = c.getChildComments().stream().map(CommentDto::toDto).collect(Collectors.toSet());
                            TreeSet<CommentDto> childTreeSet = new TreeSet<>(comparator);
                            childTreeSet.addAll(tmpSet);
                            return CommentDto.of(
                                    c.getId(), c.getContent(), c.getIsAffected(), c.getUser().getUserId(), c.getUser().getNickname(), c.getParentId(), c.getParentComment_Writer(),
                                    //자식코멘트의 자식코멘트들도 대댓글에 속하므로 대댓글과 같은 정렬로직을 적용
                                    childTreeSet,
                                    c.getCreatedDate(), c.getModifiedDate(), c.getCreatedBy()
                            );
                        }).collect(Collectors.toSet());  //Set<CommentDto>

        //초기값셋과 정렬기준(컴페레이터)를 한꺼번에 넘겨주고 싶으나 그런메서드는 없으므로 우선 정렬기준 설정 후 addAll 사용
        TreeSet<CommentDto> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(childSet);

        return new CommentDto(
                comment.getId(), comment.getContent(), comment.getIsAffected(), comment.getUser().getUserId(), comment.getUser().getNickname() , comment.getParentId(), comment.getParentComment_Writer(),
                treeSet, comment.getCreatedDate(), comment.getModifiedDate(),
                comment.getCreatedBy()
        );
    }
```

### 대댓글 출력구조-데이터는 어떻게 전송했는가
organizechilds
+기존구조의 문제점


## 대댓글 작성

-하나의 API를 통해 루트댓글저장과 대댓글저장을 한번에 처리




## jpa
## springsecurity

## 수정기능구현:javascript fetch, await

## 타임리프
