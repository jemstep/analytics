import java.io.File

import com.jemstep.commons._
import org.apache.avro.Schema
import org.apache.avro.Schema.Parser
import org.specs2.mutable.Specification

object JemstepSerializationSpecFixture {
  trait JemstepAvroSchemaT {
    val file: File
    def schema: Schema
  }

  object JemstepAvroSchemaFile {
    def apply(filename: String): JemstepAvroSchemaFile = JemstepAvroSchemaFile(new File(s"./schema/$filename.avsc"))
  }
  case class JemstepAvroSchemaFile(file: File) extends JemstepAvroSchemaT {
    def schema = new Parser().parse(file)
    def fullname = schema.getFullName
  }

  val backTestMetrics = JemstepAvroSchemaFile("backtest_metrics")
  val goal = JemstepAvroSchemaFile("goal_envelope")
  val portfolioForGoal = JemstepAvroSchemaFile("portfolio_for_goal")
  val modelQuestionnaire = JemstepAvroSchemaFile("questionnaire_event")
  val portfolioByBroker= JemstepAvroSchemaFile("portfolio_by_broker")
  val userIdentified = JemstepAvroSchemaFile("user_identified")
  val accountHolderDetails = JemstepAvroSchemaFile("account_holder_details")

  def findSchema(schemaFullname: String): Option[JemstepAvroSchemaFile] = {
    val listOfSchemas: Seq[JemstepAvroSchemaFile] = List(backTestMetrics,goal,modelQuestionnaire,portfolioByBroker,userIdentified)
    listOfSchemas.find((schema: JemstepAvroSchemaFile) => schema.fullname == schemaFullname)
  }
}

class JemstepSerializationSpec extends Specification  {

  "JemstepSerialization" should {

    "get correct names for the schemas" in {
      SchemaIO.findSchema("com.jemstep.model.assetliability.BacktestMetrics") must_== Some(JemstepSerializationSpecFixture.backTestMetrics.schema)
      SchemaIO.findSchema("com.jemstep.generator.GoalEnvelope") must_== Some(JemstepSerializationSpecFixture.goal.schema)
      SchemaIO.findSchema("com.jemstep.model.questionnaire.QuestionnaireEvent") must_== Some(JemstepSerializationSpecFixture.modelQuestionnaire.schema)
      SchemaIO.findSchema("com.jemstep.model.events.shared.UserIdentified") must_== Some(JemstepSerializationSpecFixture.userIdentified.schema)
      SchemaIO.findSchema("com.jemstep.model.enrollment.AccountHolderDetails") must_== Some(JemstepSerializationSpecFixture.accountHolderDetails.schema)
    }
  }
}