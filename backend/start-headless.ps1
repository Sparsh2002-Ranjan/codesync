$services = @(
    "api-gateway",
    "auth-service",
    "project-service",
    "file-service",
    "collaboration-service",
    "version-service",
    "execution-service",
    "comment-service",
    "notification-service"
)

Write-Host "Starting CodeSync Microservices Headless..."

foreach ($service in $services) {
    Write-Host "Starting $service..."
    $servicePath = Join-Path -Path $PSScriptRoot -ChildPath $service
    Start-Process -FilePath "mvn.cmd" -ArgumentList "spring-boot:run" -WorkingDirectory $servicePath -WindowStyle Hidden
    Start-Sleep -Seconds 4
}

Write-Host "All services started in the background."
