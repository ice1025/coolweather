#!/bin/bash
ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
yum update -y

yum install -y python-setuptools net-tools

easy_install pip

pip install --upgrade pip shadowsocks

cat>/etc/systemd/system/shadowsocks-server.service<<EOF
[Unit]
Description=Shadowsocks Server
After=network.target
[Service]
ExecStart=/usr/bin/ssserver -c /etc/ss-config.json
Restart=always
[Install]
WantedBy=multi-user.target
EOF

num=$((30000 + RANDOM))
pass=`date +%s | sha256sum | base64 | head -c 12`

cat>/etc/ss-config.json<<EOF
{
    "server_port":$num,
    "password":"$pass",
    "timeout":60,
    "method":"rc4-md5"
}
EOF


systemctl daemon-reload
systemctl enable shadowsocks-server
systemctl restart shadowsocks-server
