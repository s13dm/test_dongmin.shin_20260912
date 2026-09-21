# 테스트 진행 기록 (Post CRUD API)

로컬 환경에서 직접 테스트하기 어려운 상황이라, 원격 세션 컨테이너 안에서
Spring Boot 앱을 직접 실행(`./gradlew bootRun`)하고 `curl`로 API를 호출해
동작을 검증했다. 접근 URL은 컨테이너 내부에서만 유효한
`http://localhost:8080` 이다.

## 1. 실행 환경

| 항목 | 값 |
|---|---|
| Java | OpenJDK 21.0.10 |
| Gradle | 8.14.3 (wrapper) |
| Spring Boot | 3.3.4 |
| DB | H2 (인메모리, `jdbc:h2:mem:demo`) |
| 실행 명령 | `./gradlew bootRun --no-daemon` |

## 2. 자동 테스트 (`./gradlew clean test`)

`PostControllerTest` (MockMvc 기반) 2개 케이스 실행, 결과: **BUILD SUCCESSFUL**

- `createAndFindPost` — 게시글 생성 후 목록에서 조회되는지 확인
- `findMissingPostReturnsNotFound` — 존재하지 않는 id 조회 시 404 확인

## 3. 수동 curl 테스트 시나리오 및 결과

서버 기동 후 아래 순서대로 호출했다. (매 기동마다 H2가 초기화되므로 id는 1부터 시작)

### 3-1. GET /api/posts — 초기 빈 목록

```
$ curl http://localhost:8080/api/posts
[]
HTTP_STATUS: 200
```

### 3-2. POST /api/posts — 게시글 생성

```
$ curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"테스트 글","content":"가상서버 확인용"}'

{"id":1,"title":"테스트 글","content":"가상서버 확인용"}
HTTP_STATUS: 201
```

### 3-3. GET /api/posts — 전체 목록 조회

```
$ curl http://localhost:8080/api/posts
[{"id":1,"title":"테스트 글","content":"가상서버 확인용"}]
HTTP_STATUS: 200
```

### 3-4. GET /api/posts/1 — 단건 조회

```
$ curl http://localhost:8080/api/posts/1
{"id":1,"title":"테스트 글","content":"가상서버 확인용"}
HTTP_STATUS: 200
```

### 3-5. PUT /api/posts/1 — 수정

```
$ curl -X PUT http://localhost:8080/api/posts/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"테스트 글(수정됨)","content":"수정된 내용"}'

{"id":1,"title":"테스트 글(수정됨)","content":"수정된 내용"}
HTTP_STATUS: 200
```

### 3-6. GET /api/posts/1 — 수정 반영 확인

```
$ curl http://localhost:8080/api/posts/1
{"id":1,"title":"테스트 글(수정됨)","content":"수정된 내용"}
HTTP_STATUS: 200
```

### 3-7. DELETE /api/posts/1 — 삭제

```
$ curl -X DELETE http://localhost:8080/api/posts/1
(empty body)
HTTP_STATUS: 204
```

### 3-8. GET /api/posts/1 — 삭제 후 재조회 (404 기대)

```
$ curl http://localhost:8080/api/posts/1
Post not found: id=1
HTTP_STATUS: 404
```

### 3-9. GET /api/posts/999 — 존재하지 않는 id 조회 (404 기대)

```
$ curl http://localhost:8080/api/posts/999
Post not found: id=999
HTTP_STATUS: 404
```

### 3-10. POST /api/posts — 유효성 검증 실패 (title 빈 값, 400 기대)

```
$ curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"","content":"내용만 있음"}'

{"timestamp":"2026-09-21T08:42:40.617+00:00","status":400,"error":"Bad Request","path":"/api/posts"}
HTTP_STATUS: 400
```

## 4. 테스트 중 발견 및 수정한 이슈

**증상**: 최초 구현에서는 `PUT /api/posts/{id}`로 수정 요청을 보내면 `200 OK`와 함께
변경된 값이 응답으로 돌아오지만, 이후 `GET`으로 재조회하면 수정 전 값이 그대로 남아 있었다
(DB에 반영되지 않음).

**원인**: `PostService`에 `@Transactional`이 없어서, `update()` 안에서 엔티티의
setter로 값을 바꿔도 영속성 컨텍스트가 종료된 뒤라 변경 감지(dirty checking)가
동작하지 않고 flush가 일어나지 않았다.

**수정**: `PostService` 클래스에 `@Transactional(readOnly = true)`를 기본으로 걸고,
`create` / `update` / `delete`에 개별적으로 `@Transactional`을 추가해 쓰기 트랜잭션
안에서 flush가 일어나도록 했다. (`src/main/java/com/example/demo/post/PostService.java`)

수정 후 3-5, 3-6 시나리오를 재실행해 정상 반영됨을 확인했다.

## 5. 결론

- 자동 테스트 2건, 수동 curl 시나리오 10건 모두 기대한 상태 코드/응답을 반환.
- CRUD 전체 흐름(생성 → 조회 → 수정 → 삭제 → 404 처리)과 입력 유효성 검증(400)이
  정상 동작함을 확인.
- 수정 미반영 버그를 테스트 과정에서 발견 및 수정 완료.
