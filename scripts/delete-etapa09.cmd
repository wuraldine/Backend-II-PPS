@echo off
REM Script para eliminar la rama 'etapa09' (local y remota) de forma segura
cd /d C:\Users\DesarrolloDiseno\Documents\backend_2026 || (echo Ruta del repo no encontrada && exit /b 1)
setlocal EnableDelayedExpansion
set BR=etapa09
echo ============================
echo Repo: %CD%
echo Branch objetivo: %BR%
echo ============================
echo 1) Actualizando referencias remotas (git fetch --prune)...
git fetch --prune
echo.
echo 2) Rama actual:
for /f "delims=" %%b in ('git branch --show-current 2^>nul') do set CUR=%%b
echo %CUR%
echo.
echo 3) ¿Existe localmente %BR% ?
git branch --list %BR% >nul && (set LOCAL=1) || (set LOCAL=0)
echo Local: %LOCAL%
echo.
echo 4) ¿Existe en remoto origin/%BR% ?
git branch -r | findstr /I "%BR%" >nul && (set REMOTE=1) || (set REMOTE=0)
echo Remote: %REMOTE%
echo.
if "%CUR%"=="%BR%" (
  echo Estás en %BR%, intentar cambiar a "main"...
  git checkout main 2>nul || (
    echo No existe "main" o falló, intentar checkout a "etapa08"...
    git checkout etapa08 2>nul || (
      echo No pude cambiar de rama automáticamente. Cambia manualmente desde cmd.exe y vuelve a ejecutar este script.
      exit /b 1
    )
  )
)
echo.
if "%LOCAL%"=="1" (
  echo 5) Comprobando si %BR% está fusionada en la rama actual...
  git branch --merged | findstr /I "%BR%" >nul && (
    echo %BR% aparece como fusionada. Borrando localmente de forma segura (git branch -d %BR%)...
    git branch -d %BR%
  ) || (
    echo %BR% no está fusionada. Borrado forzado (git branch -D %BR%)...
    git branch -D %BR%
  )
) else (
  echo 5) No existe la rama local %BR%; omitiendo borrado local.
)
echo.
if "%REMOTE%"=="1" (
  echo 6) Eliminando la rama remota origin/%BR% (git push origin --delete %BR%)...
  git push origin --delete %BR% || (
    echo Falló el borrado remoto. Es posible que la rama esté protegida o ya eliminada.
  )
) else (
  echo 6) No se encontró origin/%BR% en ramas remotas; omitiendo borrado remoto.
)
echo.
echo 7) Verificación final:
git branch --list %BR%
git branch -r | findstr /I "%BR%" || echo (no hay referencias remotas encontradas para %BR%)
echo.
echo Script finalizado.
endlocal
exit /b 0

