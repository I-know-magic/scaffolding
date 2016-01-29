import com.saas.common.util.ConvertEncoding
import org.grails.cli.interactive.completers.DomainClassCompleter

import java.text.SimpleDateFormat

description( "Generates a controller & service that performs CRUD operations" ) {
  usage "grails gen-my-controller [DOMAIN CLASS]"
  completer DomainClassCompleter
  flag name:'force', description:"Whether to overwrite existing files"
}

if(args) {
  def classNames = args
  if(args[0] == '*') {
    classNames = resources("file:grails-app/domain/**/*.groovy")
                    .collect { className(it) }
  }
  for(arg in classNames) {
    def sourceClass = source(arg)   //org.grails.io.support.Resource
    def overwrite = flag('force') ? true : false
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      def timestamp = format.format(new Date())
    if(sourceClass) {
      def model = model(sourceClass)    //grails.codegen.model.Model ==> org.grails.cli.profile.codegen.ModelBuilder.model()
//        addStatus MetaClassHelper.forName(model.className).properties.toString()

        //生成Controller类
        def fileName = "grails-app/controllers/${model.packagePath}/${model.convention('Controller')}.groovy"
      render template: template('scaffolding/MyController.groovy'),
             destination: file(fileName),
             model: [className: model.className, propertyName: model.propertyName, packageName: model.packageName, classMemo: model.className, timestamp: timestamp],
             overwrite: overwrite

        //生成Service类
        fileName = "grails-app/services/${model.packagePath}/${model.convention('Service')}.groovy"
      render template: template('scaffolding/MyService.groovy'),
             destination: file(fileName),
             model: [className: model.className, propertyName: model.propertyName, packageName: model.packageName, classMemo: model.className, timestamp: timestamp],
             overwrite: overwrite

      addStatus "Scaffolding completed for ${projectPath(sourceClass)}"
    }
    else {
      error "Domain class not found for name $arg"
    }
  }
}
else {
    error "No domain class specified"
}
