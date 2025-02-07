#! /bin/bash
# elasticsearch 修改密码


# elastic 账号密码
elasticPassword=
apmSystemPassword=${elasticPassword}
kibanaSystemPassword=${elasticPassword}
logstashSystemPassword=${elasticPassword}
beatsSystemPassword=${elasticPassword}
remoteMonitoringUserSystem=${elasticPassword}
elasticsearchSetPassword() {
	echo "开始修改es密码 ... "
	sleep 1m
	sh /usr/share/elasticsearch/bin/elasticsearch-setup-passwords interactive <<EOF
y
${elasticPassword}
${elasticPassword}
${apmSystemPassword}
${apmSystemPassword}
${kibanaSystemPassword}
${kibanaSystemPassword}
${logstashSystemPassword}
${logstashSystemPassword}
${beatsSystemPassword}
${beatsSystemPassword}
${remoteMonitoringUserSystem}
${remoteMonitoringUserSystem}
EOF
}

elasticsearchSetPassword