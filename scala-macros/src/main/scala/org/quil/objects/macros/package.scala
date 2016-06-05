package org.quil.objects.macros {

  import scala.reflect.macros.whitebox
  import scala.language.experimental.macros
  import scala.annotation.StaticAnnotation


  object CrossVersionDefs {
    type CrossVersionContext = whitebox.Context
  }

  import CrossVersionDefs._

  class SQObj extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro SQObjMacro.impl
  }

  object SQObjMacro {
    def impl(c: CrossVersionContext)(annottees: c.Expr[Any]*): c.Expr[Any] = {
      import c.universe._

      def getModifiedDecl(classDecl: ClassDef) = {
        try {
          val q"case class $className(..$fields)" = classDecl

          print(className + "  " + fields + " " + classDecl)

          c.Expr(q"""

              classList = classList ::: List(classOf[${className}])
              case class $className(..$fields)(implicit val ctx: Context) extends SQObject(${className.toString()}) with SQRegistrable {}
              """
            )

        } catch {
          case _: MatchError => c.abort(c.enclosingPosition, "Annotation is only supported on case class")
        }
      }

      annottees.map(_.tree) match {
        case (classDecl: ClassDef) :: Nil => getModifiedDecl(classDecl)
        case _ => c.abort(c.enclosingPosition, "Invalid annottee")
      }
    }
  }
}