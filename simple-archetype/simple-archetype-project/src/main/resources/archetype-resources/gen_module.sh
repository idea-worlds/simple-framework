#set( $pname1 = '${packageName##*-}' )
#set( $pname2 = '${packageName##*_}' )
#set( $pname3 = '${packageName,,}' )
#set( $pname4 = '${packageName^}' )
#!/bin/bash
cd `dirname $0`
archetype_group="-DarchetypeGroupId=dev.simpleframework"
archetype_artifact="-DarchetypeArtifactId=simple-archetype-module"
archetype_version="-DarchetypeVersion=0.1-SNAPSHOT"

moduleName=$1
if [ -z $moduleName ]; then
  read -p "please input module name: " moduleName
fi
if [ -z $moduleName ]; then
  echo "module name can not be empty"
  exit 1;
fi

packageName=$moduleName
packageName=${pname1}
packageName=${pname2}
packageName=${pname3}
sampleName=${pname4}

module_group="-DgroupId=${groupId}"
module_artifact="-DartifactId=$moduleName"
module_version="-Dversion=${version}"
module_package="-Dpackage=${package}.$packageName"
module_sample="-Dsample=$sampleName"
module_project="-Dproject=${artifactId}"
module_db="-Ddb=${db}"

mvn archetype:generate $archetype_group $archetype_artifact $archetype_version $module_group $module_artifact $module_version $module_package $module_sample $module_project $module_db