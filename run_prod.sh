sudo nohup java -jar -Dspring.profiles.active=prod target/transitnet-0.0.1-SNAPSHOT-execute.jar  &
echo "后台运行中，输入以下命令查看日志："
echo "sudo tail -f nohup.out"