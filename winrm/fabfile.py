import winrm

def ipconfig(host,username,passwd):
  s = winrm.Session(host, auth=(username, passwd), transport='basic')
  r = s.run_cmd('ipconfig',['/all'])
  print r.status_code
  print r.std_out
  print r.std_err
