#!/bin/bash -x

pushd classes-eclipse
	jar cvf ../dellroad-stuff-vaadin-2.0.jar .
popd
cp dellroad-stuff-vaadin-2.0.jar ~/.m2/repository/org/dellroad/dellroad-stuff-vaadin/2.0/
