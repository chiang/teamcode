

# Python Fabric

## Installation

먼저 fabric 을 설치해야 합니다. 물론 Python 은 설치되어 있어야 합니다.

```
# docker run --rm -it --name test-fabric teamcode/default-image:latest
# mkdir -p /home/fabric
# cd /home/fabric
# vi fabfile.py
```

다음으로 아래 스크립트를 만듭니다.

```
import winrm

def ipconfig(host,username,passwd):
  s = winrm.Session(host, auth=(username, passwd), transport='basic')
  r = s.run_cmd('ipconfig',['/all'])
  print r.status_code
  print r.std_out
  print r.std_err

```

그리고 다음과 같이 실행합니다.

```
fab ipconfig:'45.77.20.69','chiang','****'
```

그러면 Connection Refused 오류가 발생할 겁니다. 당연합니다. 이제 WinRM 설정을 해야 할 때입니다. ``winrm.md`` 파일을 참고하세요.



```
brew install fabric
```

다음으로 winrm 패키지를 설치합니다.

fabfile.py 를 Root Directory 에 두면 읽지 못한다. 참고 바람.

## 서버 재기동

```
script:
  - fab stop:
  - sleep 10s
  - fab start:

```

## Windows 연동

아래 스크립트는 D 드라이브로 이동 후 VisangNAS 라는 디렉터리로 이동하여 새로운 파일 하나를 생성하는 스크립트입니다.

```cmd
import winrm

def ipconfig(username):
  s = winrm.Session('****', auth=(username, '****'), transport='basic')
  r = s.run_cmd('cd /d d: && cd data && type NUL > 1.txt && dir',[])
  print r.status_code
  print r.std_out
  print r.std_err
```

실행은 아래와 같이 합니다.

```
fab deploy:username='******',password='******' 
```

## Windows Fundamentals

```python
import winrm

def deploy(host,username,password):
  s = winrm.Session(host, auth=(username, password), transport='basic')
  r = s.run_cmd('cd /d d: && dir',[])
  print r.status_code
  print r.std_out
  print r.std_err

def stop(host,username,password):
  s = winrm.Session(host, auth=(username, password), transport='basic')      
	r = s.run_ps('Stop-Service FreeSSHDService')
	print r.status_code
	print r.std_out
	print r.std_err

def start(host,username,password):
  s = winrm.Session(host, auth=(username, password), transport='basic')
  r = s.run_ps('Start-Service FreeSSHDService')
  print r.status_code
  print r.std_out
  print r.std_err

```


```bash
fab deploy:'10.10.10.10','*****','*****'
```

## PowerShell 살펴보기

기본적으로 명령어 실행은 Powershell 의 Cmdlet 을 활용합니다. Cmdlet (Command-let) 은 Windows PowerShell 에서 사용되는 기본적인 명령어 입니다.

명령어는 “동사-명사”의 형태로 구성되어 있으며, 대/소문자를 구분하지 않습니다.

Cmdlet의 기본 구문은 아래와 같습니다.

```
  동사-명사 매개변수 인자
```

위와 규칙에 따라서 Windows service 를 실행, 중지하는 Cmdlet 은 아래와 같습니다 (위 예시에서 사용한 것처럼).

```
Stop-Service
Start-Service
```

If you have powershell 5.1 you also have :

```
Stop-Service -Name "NameOfService" -NoWait
```


### 다른 드라이브로 이동하기

```
cd /d d: && dir
```

http://www.microsoft.com/resources/documentation/windows/xp/all/proddocs/en-us/ntcmds_shelloverview.mspx?mfr=true

Using multiple commands and conditional processing symbols

You can run multiple commands from a single command line or script using conditional processing symbols. When you run multiple commands with conditional processing symbols, the commands to the right of the conditional processing symbol act based upon the results of the command to the left of the conditional processing symbol.

For example, you might want to run a command only if the previous command fails. Or, you might want to run a command only if the previous command is successful.

You can use the special characters listed in the following table to pass multiple commands.

& [...]  command1 & command2
Use to separate multiple commands on one command line. Cmd.exe runs the first command, and then the second command.

&& [...]  command1 && command2
Use to run the command following && only if the command preceding the symbol is successful. Cmd.exe runs the first command, and then runs the second command only if the first command completed successfully.

|| [...]  command1 || command2
Use to run the command following || only if the command preceding || fails. Cmd.exe runs the first command, and then runs the second command only if the first command did not complete successfully (receives an error code greater than zero).

( ) [...]  (command1 & command2)
Use to group or nest multiple commands.

; or , command1 parameter1;parameter2
Use to separate command parameters.