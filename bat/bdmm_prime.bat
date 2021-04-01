REM Check whether the JRE is included
IF EXIST %~dp0\..\jre (

REM for BEAST version that includes JRE
    %~dp0\..\jre\bin\java -cp %~dp0\..\out\artifacts\bdmm_prime_jar\bdmm-prime.jar beast.app.beastapp.BeastMain %*

) ELSE (
REM for version that does not include JRE
    java -jar %~dp0\..\out\artifacts\bdmm_prime_jar\bdmm-prime.jar %*
)

::"C:\Users\rengg\Desktop\ETH\BEAST\Beast Code\bdmm-prime\out\artifacts\bdmm_prime_jar\bdmm-prime.jar"