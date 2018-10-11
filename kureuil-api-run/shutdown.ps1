
$tcp = New-Object System.Net.Sockets.TcpClient

$tcp.Connect("127.0.0.1", 2848)

$tcp.Close()
