var fs = require('fs');
var os = require('os');

var rewritePackage = function(path, packageName) {
    var file = fs.readFileSync(path).toString().split(os.EOL);
    var index = file.indexOf('package com.straphq.androidwearsampleprojectreal;');
    file[index] = 'package ' + packageName + ';';
    fs.writeFileSync(path, file.join(os.EOL));

}

    var rewriteManifest = function(path, packageName) {
	var file = fs.readFileSync(path).toString().split(os.EOL);
	var index = file.indexOf('    package="com.straphq.androidwearsampleprojectreal" >');
	file[index] = '    package="' + packageName + '" >';
	fs.writeFileSync(path, file.join(os.EOL));
    }


    var pkgName = 'com.straphq.rewrite';
var dirName = './mobile/src/main/java/com/straphq/androidwearsampleprojectreal/';
    var files = fs.readdirSync(dirName);

files.forEach(function(file) {
	rewritePackage(dirName + file, pkgName);
});
dirName = './wear/src/main/java/com/straphq/androidwearsampleprojectreal/';
files = fs.readdirSync(dirName);
files.forEach(function(file) {
        rewritePackage(dirName + file, pkgName);
    });
rewriteManifest('./wear/src/main/AndroidManifest.xml', pkgName);
