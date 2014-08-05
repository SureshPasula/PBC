#!/usr/bin/expect -f

set TARGET_USER [lindex $argv 0]
set TARGET_PASSWORD [lindex $argv 1]
set TARGET_HOST [lindex $argv 2]
set SOURCE_PATH [lindex $argv 3]
set TARGET_PATH [lindex $argv 4]

spawn scp $SOURCE_PATH "$TARGET_USER@$TARGET_HOST:$TARGET_PATH"

expect {
authenticity {send "yes\r"; exp_continue}
}

expect {
password: {send "$TARGET_PASSWORD\r"; exp_continue}
}