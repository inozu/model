package com.innovenso.townplanner.model.concepts.properties

import com.innovenso.townplanner.model.meta.{Key, SortKey}

case class DataClassification(
    level: DataClassificationLevel,
    description: Option[String] = None
) extends Property {
  val key: Key = Key("data classification")
  val sortKey: SortKey = SortKey.next
  val canBePlural: Boolean = false
}

trait DataClassificationLevel {
  def title: String
  def level: Int
}

case object PublicData extends DataClassificationLevel {
  val title: String = "Public"
  val level = 0
}

case object ConfidentialData extends DataClassificationLevel {
  val title: String = "Confidential"
  val level = 1
}

case object SensitiveData extends DataClassificationLevel {
  val title: String = "Sensitive"
  val level = 2
}

case object PersonalData extends DataClassificationLevel {
  val title: String = "Personal"
  val level = 3
}

object DataClassification {
  def fromString(
      level: String,
      description: Option[String] = None
  ): DataClassification = DataClassification(
    level = level match {
      case "Public" | "public" | "0"             => PublicData
      case "Confidential" | "confidential" | "1" => ConfidentialData
      case "Sensitive" | "sensitive" | "2"       => SensitiveData
      case "Personal" | "personal" | "3"         => PersonalData
      case _                                     => SensitiveData
    },
    description = description
  )
}

trait HasDataClassification extends HasProperties {
  def dataClassification: Option[DataClassification] = props(
    classOf[DataClassification]
  ).headOption
  def withDataClassification(
      dataClassification: DataClassification
  ): HasProperties =
    withProperty(dataClassification)
}

trait CanConfigureDataClassification[
    ModelComponentType <: HasDataClassification
] {
  def propertyAdder: CanAddProperties
  def modelComponent: ModelComponentType

  def has(dataClassification: DataClassification): ModelComponentType =
    propertyAdder.withProperty(modelComponent, dataClassification)

  def isClassified(
      dataClassificationLevel: DataClassificationLevel
  ): ModelComponentType = has(
    DataClassification(level = dataClassificationLevel)
  )

  def isClassified(
      dataClassificationLevel: DataClassificationLevel,
      description: String
  ): ModelComponentType = has(
    DataClassification(
      level = dataClassificationLevel,
      description = Some(description)
    )
  )
}
