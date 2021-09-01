# WinRM

## 설정하기 

일반 사용자로 로그인한 경우를 가정합니다. PowerShell 를 관리자 권한으로 실행합니다. 그 후 아래 명령어를 입력합니다.

```
Enable-PSRemoting -Force
```

만약 이미 설정된 경우라면 아래와 같은 메시지가 출력됩니다.

```
WinRM is already set up to receive requests on this computer.
WinRM is already set up for remote management on this computer.
```

그리고 Powershell 에서 아래 명령어를 실행합니다.

```
gpedit.msc
```

서비스를 찾습니다.

1. 기본 인증 허용 항목을 찾아 허용합니다.
2. ``호환성 HTTP 수신기 사용`` 항목을 찾아 사용 설정합니다. 기본 포트는 5985 입니다.
3. ``암호화 안 된 트래픽 허용`` 항목을 찾아 허용합니다. 이 항목을 설정하지 않으면 WinRM 을 통해 접속 시 ``the specified credentials were rejected by the server`` 오류 메시지를 받게 됩니다.

이제 다시 Fabric 을 실행해 봅니다. 여전히 같은 오류가 발생합니다. ``5985`` 포트에 대한 방화벽을 오픈해 줘야 합니다. Windows 서버에 가서 관리 도구를 실행한 다음 방화벽 설정에서 Inbound 규칙으로 ``5985`` 포트를 추가해 줍니다. 

이제 다시 Fabric 을 실행해 봅니다. 이전 같은 오류는 발생하지 않습니다. 다만 아래와 같은 새로운 오류 메시지가 출력됩니다.

```
winrm.exceptions.InvalidCredentialsError: the specified credentials were rejected by the server
```

우리 사용자 계정이 권한이 부족해서 생기는 문제입니다. Administrator 계정이면 문제가 없겠지만 그렇게 실행하는 것이 좋지 않으므로 이와 같이 실행합니다.

Basic 인증 시 아래와 같이 사용자를 추가할 수 있습니다. 여기서 추가란 실제 사용자를 추가하는 것이 아니고 로컬 컴퓨터에 등록된 사용자를 WinRM 사용자로 등록하는 과정을 의미합니다. Basic 인증에는 Domain User 를 사용할 수 없습니다. 

WINRM에 대한 사용자 인증 구성. WinRM에 대한 사용자 인증을 구성하려면 다음을 수행하십시오.

1. Windows 메뉴에서 Start(시작) > Run(실행) 을 클릭합니다.
2. Run(실행) 창에 winrm configsddl default를 입력하고 OK(확인)를 클릭합니다. 그러면 Dialog 가 표시됩니다.
3. Add(추가)를 클릭하여 필요한 로컬 또는 도메인 사용자나 그룹을 목록에 추가합니다.
4. 각 사용자에게 적절한 권한을 제공하고 OK(확인)를 클릭합니다 (적절한 권한이란 Invoke 가 들어가야 함을 의미? 합니다. 외부에서 호출하게 합니다).

``winrm configsdll default`` 는 ``winrm`` 을 사용할 수 있는 권한 그룹에 사용자를 추가하는 명령어입니다. 


이제 다시 실행해 봅니다. 아쉽게도 아직 아래와 같은 오류가 발생합니다.

```
winrm.exceptions.WinRMTransportError: (u'http', u'Bad HTTP response returned from server. Code 500')
```

우리가 ``읽기`` 권한만 부여했기 때문에 추가로 Execute (Invoke) 권한을 주어야 ``ipconfig`` 명령어를 실행 (Execute or Invoke) 할 수 있습니다. 이제 다시 실행하면 아래와 같이 성공적인 메시지를 볼 수 있습니다.

```
Windows IP Configuration

   Host Name . . . . . . . . . . . . : windows-dev
   Primary Dns Suffix  . . . . . . . : 
   Node Type . . . . . . . . . . . . : Hybrid
   IP Routing Enabled. . . . . . . . : No
   WINS Proxy Enabled. . . . . . . . : No

Ethernet adapter Ethernet 2:

   Connection-specific DNS Suffix  . : 
   Description . . . . . . . . . . . : Red Hat VirtIO Ethernet Adapter #2
   Physical Address. . . . . . . . . : **-**-**-**-**-**

...
...
...
```
