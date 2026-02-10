# go inside resource pack
pushd src/resource_pack

# get version from from pom.xml
version="$(grep -oPm1 "(?<=<version>)[^<]+" ../../pom.xml)"
filename="aurivale_resource_pack-${version}.zip"

# generate zip
zip -x *.xcf -r ${filename} *

# make sure target folder existrs
target="../../target/"
if [ ! -d $target ]; then
  mkdir $target
fi

# mv zip
mv "${filename}" $target

# move back to original directory
popd
