#!/bin/bash

CONTINUUM_BIN=/cmjp/jboss/continuum/bin

$CONTINUUM_BIN/continuum stop
sleep 10
$CONTINUUM_BIN/continuum start

