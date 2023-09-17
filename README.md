# nestedCommentService

* 개발할 때 사고의 흐름등을 주석으로 자주 기록해놓는 편인만큼 첫 깃허브사용이라 주석 삭제 및 사소한 수정으로 인해 쓸데없이 변경내용이 상당히 많이 잡혔다. 다음부턴 개선하도록 하자

* 리마인드를 위해 구현기술 설명해봄 


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

---

## 무한대댓글 출력구조와 정렬

### 출력구조

이중루프를 통해 구현하였다.

우선 1차적으로 루트댓글들을 순회하며 출력하고, 해당 루프안에서 현재 댓글에 자식댓글이 있다면 또 순회하며 자식댓글을 출력한다.

자식댓글을 출력하며 마지막에 한번 더 자식댓글에 자식댓글이 있는지 검사하고,

있다면 Thymeleaf의 fragment와 replace기능으로 재귀호출을 통해 구현하였다. 

전체적인 구조는 아래와 같다.

```html
*** index.html

<div th:if="!${commentList.isEmpty()}" th:each="comment:${commentList}" class="d-flex mb-1">
                     ...댓글출력부분
                     <div th:if="!${comment.childComments().isEmpty()}">
                        <th:block th:replace="::grandComment(${comment.childComments()})"></th:block>
                     </div>
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

우선 컨트롤러에서 뷰쪽으로 넘겨주는 commentList에는 루트댓글들만 넘겨준다. 루트댓글들만 넘겨주어도 루트댓글이 자식댓글을 가지고있고, 뷰에서 재귀 및 반복을 통해 호출할 수 있기 때문에 이렇게 구현했다.

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

여기서 중요한점은 화면 출력 시 정렬은 db에 저장되어있는 객체들의 정렬방식을 따르지 않게 되었기 때문에, 별도로 이렇게 한번 더 DTO차원에서 정렬을 해준 것이다. (개발하며 이 부분을 몰라 상당히 많은 시간이 소요됐었다)

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

---

## 댓글 CRUD

### 댓글 등록하기

* 댓글 등록 : POST /save

* 대댓글 등록 : POST /save/{parnetCommentId}

둘 다 서비스계층(CommentService.java)의 saveComment()를 사용하도록 구현 함. 등록할 댓글이 루트댓글이라면 isAffected와 parentCommentId는 넘어오지 않으니, Optional처리하여 서비스로 넘겨준다. 자식 댓글일 때는 둘 다 넘어옴.

```java
***CommentController.java

@PostMapping( "/save")
    public String saveComment(
            @RequestParam String content,
            @AuthenticationPrincipal UserAccountPrincipal principal
        )
    {
        commentService.saveComment(content, Optional.empty(), principal.getUsername(), Optional.empty());
        return "redirect:/";
    }

@PostMapping( "/save/{parentCommentId}")
    public String saveChildComment(
            @RequestParam String content,
            @RequestParam String isAffected,
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable(required = false) String parentCommentId
        )
    {
        commentService.saveComment(content, Optional.of(isAffected.equals("1") ? true : false), principal.getUsername(), Optional
                .ofNullable(Long.parseLong(parentCommentId)));
        return "redirect:/";
    }
```

서비스에서는 컨트롤러에서 넘어온 두 Optional값을 통해 등록될 댓글이 대댓글인지 판단하고,

대댓글일때 부모댓글에 이 댓글을 자식으로 등록해주고, 자식댓글은 부모댓글작성자 출력을 위한 변수 parentComment_Writer 세팅을 해줌



```java
***CommentService.java

public void saveComment(String content, Optional<Boolean> isAffected, String userId, Optional<Long> parentCommentId) {
        //userid를 통해 user 가져옴
        UserAccount user = userAccountRepository.findByUserId(userId).get();

        if (parentCommentId.isPresent()) {
            String parentComment_Writer = commentRepository.findById(parentCommentId.get()).get().getCreatedBy();
            log.info("(서비스) commentRepository.findById(parentCommentId.get()).get()"+commentRepository.findById(parentCommentId.get()).get());
            Comment targetComment = Comment.of(content, isAffected, user, parentCommentId, parentComment_Writer);
            Comment parentComment = commentRepository.getReferenceById(parentCommentId.get());
            parentComment.addChildComment(targetComment);
            log.info("parentComment.addChildComment(targetComment); 실행 후 " + parentComment.getId() + "의 자식: " + parentComment.getChildComments());
            commentRepository.save(targetComment);
        }
        else {
            Comment targetComment = Comment.of(content, isAffected, user, parentCommentId, null);
            commentRepository.save(targetComment);
        }
    }
```

* details와 summary 태그를 통해 대댓글달기버튼처럼 활용
![image](https://github.com/LimJungSub/nestedCommentService/assets/80201699/043f15b1-cd8a-4c08-9248-2c016ca8124d)

* 누구에게 대댓글을 달았는지, 즉 부모댓글의 작성자 parentComment_Writer 를 앞에 표시
![image](https://github.com/LimJungSub/nestedCommentService/assets/80201699/52ae7137-6821-4e10-9e3f-1362cc0fc08c)



### 댓글 수정하기

추가 비밀번호 검증과정을 거쳐 작성자 본인이 맞는지 재확인 후 수정폼 띄움.

![image](https://github.com/LimJungSub/nestedCommentService/assets/80201699/a740a1c9-ac27-4ced-ac12-7cfe04bb1152)

JavaScript의 fetch API를 통해 구현함.

비밀번호 일치 시 response.ok 반환받음, 아래와 같이 기존 내용 폼에 그대로 담아 출력하여 기존내용을 바탕으로 수정할 수 있게함. 

수정폼이 숨겨져있다가 나타나는 방식(자바스크립트를 통한 hidden속성 해제)으로 구현함. 
![image](https://github.com/LimJungSub/nestedCommentService/assets/80201699/564e3db0-e80f-44c6-b5ce-3dd1d35108a8)

```javascript
function getUpdateForm(num, commentId) {
        //target = 변경대상, 폼으로 대체될 부분
        const targetContentTag = num == 1 ? "viewContent-" + commentId : "childViewContent-" + commentId;
        const targetIsAffectedTag = num == 1 ? null : "childViewIsAffected-" + commentId;
        const updatingFormFragment = num == 1 ? "rootUpdatingForm-" + commentId : "childUpdatingForm-" + commentId;
        const enteredPassword = prompt("수정하시겠습니까?\n\n비밀번호를 입력하세요.");

        fetch("/password-checker", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(enteredPassword)
        }).then(
            response => {
                if (response.ok) {
                    const requestUrl = '/' + commentId + '/update';
                    const target = num == 1 ? "rootUpdatingFormSubmit-" + commentId : "childUpdatingFormSubmit-" + commentId;
                    const form = document.getElementById(target);
                    form.action = requestUrl;
                    // 액션값까지만 지정해주고, 등록버튼을 누르면 제출이벤트가 발생되도록 구현하면된다. 


                    //비밀번호 제출 후 - 사용자의 입력을 받기

                    //원댓글의 Content 가져오기
                    let baseCommentDiv = document.getElementById(targetContentTag);
                    const baseCommentContent = baseCommentDiv.innerText;

                    if (num == 1) {
                        document.getElementById("rootUpdatingContent-" + commentId).innerText = baseCommentContent;
                    } else if (num == 2) {
                        document.getElementById("childUpdatingContent-" + commentId).innerText = baseCommentContent;
                    }
                    
                    //원댓글의 isAffected 가져오기
                    if (targetIsAffectedTag != null) {
                        const val = document.getElementById(targetIsAffectedTag).innerText;
                        if (val === "false") {
                            document.getElementById("childIsAffectedRem-" + commentId).setAttribute("checked", "");
                        } else if (val === "true") {
                            document.getElementById("childIsAffectedDel-" + commentId).setAttribute("checked", "");
                        }
                    }
                    if (num == 1) {
                        document.getElementById(targetContentTag).setAttribute("hidden", "");
                        document.getElementById(updatingFormFragment).removeAttribute("hidden");
                    } else if (num == 2) {
                        document.getElementById(targetContentTag).setAttribute("hidden", "");
                        document.getElementById(updatingFormFragment).removeAttribute("hidden");
                    }
                } else {
                    alert("자바스크립트: 비밀번호 불일치");
                }
            }
        )
            .catch(
                () => {
                    alert("서버와 통신 중 에러가 발생하였습니다.");
                }
            );
}
```

위 javascript코드에서 활용햐는 controller 코드들
```java
***CommentController.java

@PostMapping("/password-checker")
    public ResponseEntity<String> passwordChecker(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @RequestBody String password
    )
    {
        String passwordRemovedQuotes = password.substring(1, password.length()-1);
        //이렇게 따옴표 없애는 과정을 안하려면? 디티오를 써라. 알아서 매핑해줄테니 (지금은 안함), 원노트 참고
        if(principal.getPassword().equals("{noop}"+passwordRemovedQuotes)){
            return ResponseEntity.ok("확인");
        }
        else{
            return ResponseEntity.badRequest().body("일치하지 않는 비밀번호입니다. 입력하신 비밀번호: " + passwordRemovedQuotes);
        }
    }
    

    @PostMapping("/{commentId}/update")
    //수정가능한 내용 : 콘텐트, 캐스캐이딩 여부 - form의 post로 들어옴
    public String updateComment(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long commentId,
            @RequestParam String updatingContent,
            @RequestParam(required = false) Boolean updatingIsAffected
    ){
        commentService.updateComment(commentId, updatingContent, updatingIsAffected);
        return "redirect:/";
    }
```


이후 실제 Update 기능은 JPA의 DirtyChecking을 활용하여 구현
```java
***CommentService.java

public void updateComment(Long commentId, String content, Boolean isAffected) {
        //Dirty checking을 활용하여, 엔티티를 가져오고 수정한다.
        Comment comment = commentRepository.getReferenceById(commentId);
        if (comment.getContent() != null) {
            comment.setContent(content);
        }
        if (comment.getIsAffected() != null) {
            comment.setIsAffected(isAffected);
        }
    }
```

### 수정기능 구현하며 깨달은 점

기능을 구현하며 문제를 겪었던 점은

* 문제1 : 기존 댓글의 내용 못가져옴

* 문제2:  수정폼뜨는위치가 맞지 않음

사진으로보면 아래와 같았다.

![image](https://github.com/LimJungSub/nestedCommentService/assets/80201699/fcfa67fd-4b59-4d63-8a31-e96d069b939b)


* 해결: 수정버튼을 누르는 시점은 이미 다 댓글들이 뷰에 렌더링 된 후 이기에 정확히 어떤 코멘트인지를 잡아주지 못해서 그랬었음.

* 각 댓글이 렌더링 되는 쪽에 id를 부여해서 해결할 수 있었는데, 단순히 viewIsAffected와 viewContent에만 id를 붙여주는 것이 아닌, 숨겨졌던 updatingForm쪽에도 id를 붙여줘서 해결했어야했음. 위 js코드를 보면 변수들에 +commentId가 붙어있는데 해결 결과로써 나타난 코드이다.



### 댓글 삭제하기

댓글수정과는 살짝다르게 async, await를 활용하여 구현해봄

```javascript
async function deleteComment(commentId) {
        try {
            const enteredPassword = prompt("삭제하시겠습니까?\n\n비밀번호를 입력하세요.");
            const deleteResponse = await fetch('/' + commentId + '/delete', {    
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(enteredPassword)
            });
            console.log("deleteResponse값을 판단하는 제어문을 탑니다");
            if (deleteResponse.ok) {
                console.log("deleteResponse값: ok");
                location.reload(); //전제페이지를 리로드
            } else {
                const errorTextDeleting = await deleteResponse.text();
                throw new Error(errorTextDeleting);
            }
            console.log("deleteResponse: " + deleteResponse.status);
        } catch (error) {
            console.log(error);
        }
    }
```


```java
@PostMapping("/{commentId}/delete")
    public ResponseEntity<String> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @RequestBody String password
    ) {
        log.info(commentId);
        String passwordRemovedQuotes = password.substring(1, password.length()-1);
        if (principal.getPassword().equals("{noop}"+passwordRemovedQuotes)) {   //비교시에 SpringSecurity암호화로 인해 붙이는 {noop}까지 포함하여 비교하여야한다.
            commentService.deleteComment(Long.parseLong(commentId));
            return ResponseEntity.ok("댓글이 삭제되었습니다 commentId: "+Long.parseLong(commentId));
        } else {
            return ResponseEntity.badRequest().body("일치하지 않는 비밀번호입니다. from 컨트롤러");
        }
    }
```

* 아래는 cascade옵션이 설정되어 있는 대댓글들을 삭제하기 위한 코드이다. 
```java
public void deleteComment(Long commentId) {
        //아이디로 객체를 가져와서 자식을 가져오고 자식set을 순회하며 해당엔티티의 삭제여부를 결정
        Comment targetComment = commentRepository.findById(commentId).get();
        targetComment.getChildComments().stream().forEach(comment -> {
                    if (comment.getIsAffected() == true) {
                        commentRepository.deleteById(comment.getId());
                    }
                }
        );
        commentRepository.deleteById(commentId);

    }
```
