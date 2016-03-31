#!/bin/sh
mvn -Dwildfly.keepalive=true com.edugility:h2-maven-plugin:spawn wildfly:start