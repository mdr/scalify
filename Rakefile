require 'find'

PLUGINS = 
  if (ENV["ECLIPSE_HOME"] && File.exists?(ENV["ECLIPSE_HOME"] + "/plugins")) 
    then ENV["ECLIPSE_HOME"] + '/plugins'
    else 'osgi/plugins'
  end
  
LIBS="#{PLUGINS}/\* lib/configgy.jar lib/scalaz.jar lib/scalax.jar"
CP = "-classpath " + LIBS.split.join(":")
DESTPATH = "target/classes"
SOURCEPATH = 'src/main/'
FSC = 'fsc -deprecation -unchecked'

JAVAOPTS = "-Xms512M -Xmx1g -Xss4m"
# -verbose -Xfuture -Xcheckinit -Xprint:typer -Ybrowse:typer

VMARGS = "-vmargs -XX:+UseParallelGC"
# -d64

OSGIJAR = "osgi/org.improving.scalify.osgi.jar"
OSGIBND = "osgi/org.improving.scalify.osgi.bnd"
OSGIOPTS = "-jar org.eclipse.osgi_3.4.0.v20080605-1900.jar -configuration configuration -console"
BND = "java -jar lib/bnd.jar -exceptions"

src = Array.new
Find.find("#{SOURCEPATH}") do |path|
  if (path =~ /\.scala$/) then
    src << path
  end
end
srcs = src.join(" ")
# srcs = `find #{SOURCEPATH} -newer #{OSGIJAR} -print`.split.join(" ")

task :default => [:codegen, :build, :osgi]
task :force => [:assault, :clean, :default]
# task :default => [:codegen, :osgi]

task :build do
  if (!File.exist?(DESTPATH)) then
    FileUtils.mkdir_p DESTPATH
  end
  
  sh "#{FSC} #{CP} #{srcs} -d #{DESTPATH}"
  sh "jar cMf lib/scalify.jar -C target/classes scalify"
end

task :osgi do
  if (!File.exist?(OSGIJAR)) then
    sources = `find #{SOURCEPATH} -print`.split 
  else 
    sources = `find #{SOURCEPATH} -newer #{OSGIJAR} -print`.split 
  end
  
  if (!File.exist?(OSGIJAR) || sources.size > 0) then
    task(:build)
    sh "#{BND} build #{OSGIBND}"
    sh "cp #{OSGIJAR} #{PLUGINS}"
    system("rm -rf osgi/workspace")
    if (!File.exist?("log")) then
      FileUtils.mkdir_p "log"
    end
    system("cp /dev/null log/scalify.log")
  else
    puts "Bundle is up to date."
  end
end

task :codegen do
  pairs = { "bin/extractors.scala" => "#{SOURCEPATH}codegen/GenExtractors.scala",
            "bin/tokens.scala" => "#{SOURCEPATH}codegen/Tokens.scala",
            "bin/wrappers.scala" => "#{SOURCEPATH}codegen/GenWrappers.scala"
          }
  pairs.each do |gen, out|
    if (!File.exists?(out) || (File.mtime(gen) > File.mtime(out))) then
      puts("Regenerating #{out}...")
      system("#{gen} > #{out}")
    else
      puts("Source file #{out} is up to date. ")
    end
  end
end

JPROFILEDIR = "/local/lib/java/jprofile"
JPROFILE = "-javaagent:#{JPROFILEDIR}/profile.jar -Dprofile.properties=#{JPROFILEDIR}/profile.properties"
JREBEL = "-noverify -javaagent:/local/lib/java/javarebel.jar"
JHAT = "-agentlib:hprof=cpu=samples,interval=20,depth=3"

task :console do
  Dir.chdir("osgi")
  sh "rlwrap -c java #{JAVAOPTS} #{OSGIOPTS} #{VMARGS}"
  Dir.chdir("..")
end

task :clean do
  system("rm -rf target")
  system("rm -rf osgi/configuration/org.*")
  system("rm -rf osgi/workspace")
end

task :assault do
  system("killall java")
end
