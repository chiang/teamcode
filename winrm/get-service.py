import winrm

s = winrm.Session('1.1.1.1', auth=('****', '****'))
r = s.run_ps('Get-Service XblGameSave')
print r.status_code
print r.std_out
print r.std_err
