
## Audit Events

아래로 보이는데 흠.. 정확치는 않은 듯.

https://docs.gitlab.com/ee/administration/audit_events.html

### 사용자 접속 이력 이벤트

1. /app/controllers/sessions_controller.rb 에서 처음 시작
2. create 함수가 세션을 생성하는 함수이며 여기서 log_audit_event 호출
3. log_audit_event 함수에서는 AuditEventService.new 를 호출

Audit Events 가 아래와 같은데,

```
create_table "audit_events", force: :cascade do |t|
    t.integer "author_id", null: false
    t.string "type", null: false
    t.integer "entity_id", null: false
    t.string "entity_type", null: false
    t.text "details"
    t.datetime "created_at"
    t.datetime "updated_at"
end
```

기록을 남길 때는 아래와 같습니다.

사용자 프로필 화면 중 Audit Log 화면에서는 아래와 같은 기록을 보여줍니다.

Signed in with standard authentication 1 day ago

위 정보는 아래와 같이 매핑이 됩니다.

```
author_id = User.class.id (이 데이터를 생성한 사람인데 얘와 entity_id 는 동일) 
type = ?
entity_id = User.class.id
entity_type = 'User'
details = 'standard'
created_at = timestamp (얘를 1 day ago 로 변환)
updated_at = 얘는 들어갈 일이 없겠다.
```

위와 같다면 아래와 같이 Render 합니다 (파일은 /app/views/profiles/_event_table.html.haml).
```
%h5.prepend-top-0
  History of authentications
%ul.well-list
  - events.each do |event|
    %li
      %span.description
        = audit_icon(event.details[:with], class: "append-right-5")
        Signed in with
        = event.details[:with]
        authentication
      %span.pull-right
        #{time_ago_in_words event.created_at} ago
= paginate events, theme: "gitlab"
```

화면에 출력하는 문자열을 유형에 따라 조합하는 형태를 사용합니다.

위의 events 는 아래와 같이 조회합니다 (파일은 /app/controllers/profiles_controller.rb).

```
def audit_log
    @events = AuditEvent.where(entity_type: "User", entity_id: current_user.id).
      order("created_at DESC").
      page(params[:page])
  end

```


## Artifacts

build.rb 파일에서 erase 라는 함수로 제거합니다. erase 함수는 아래와 같습니다.

```ruby
def erase(opts = {})
  return false unless erasable?

  erase_artifacts!
  erase_trace!
  update_erased!(opts[:erased_by])
end
```

삭제할 대상인지 여부는 아래와 같이 확인합니다.
```ruby
def erased?
  !self.erased_at.nil?
end
```

위 작업은 ``ExpireBuildArtifactsWorker`` 파일이 처리합니다. 해당 파일에서는 ``build.rb`` 에서 아래 ``scope`` 으로 처리합니다.

```ruby
...
scope :with_artifacts, ->() { where.not(artifacts_file: [nil, '']) }
scope :with_expired_artifacts, ->() { with_artifacts.where('artifacts_expire_at < ?', Time.now) }
...
```

삭제할 때 ``update_erased`` 에서 ``artifacts_expire_at`` 을 ``null`` 처리하기 때문에 위의 Scope 에서 걸리지 않습니다. 

```ruby
def update_erased!(user = nil)
  self.update(erased_by: user, erased_at: Time.now, artifacts_expire_at: nil)
end
```

### Browsing Artifacts

Artifacts 파일이 Archive 이지만 이 파일 내부 정보를 보고 싶을 수 있습니다. Gitlab 은 이 기능을 제공합니다. 어떻게 하나? 
파일을 업로드할 때 Metadata 라는 것을 정의해서 이 정보를 데이터베이스에 저장하고 사용자가 요청할 때 보여줍니다. 이 파일은 아래 경로에 있습니다

```
lib/gitlab/ci/bild/artifactsFile/metadata.rb
```

정보는 JSON 으로 저장합니다. 

### Dependencies 



