@echo off
echo =============================================
echo  Abrindo portas do E.D.E.N. no Firewall
echo  Execute como ADMINISTRADOR!
echo =============================================

netsh advfirewall firewall add rule name="EDEN TCP 5000 Entrada" protocol=TCP dir=in localport=5000 action=allow
netsh advfirewall firewall add rule name="EDEN UDP 5000 Entrada" protocol=UDP dir=in localport=5000 action=allow
netsh advfirewall firewall add rule name="EDEN UDP 5001 Discovery" protocol=UDP dir=in localport=5001 action=allow

echo.
if %errorlevel%==0 (
    echo [OK] Portas 5000 TCP, 5000 UDP e 5001 UDP liberadas!
) else (
    echo [ERRO] Falha. Certifique-se de executar como Administrador.
)
echo =============================================
pause
