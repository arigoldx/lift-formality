package com.hacklanta
package formality

import scala.xml._

import org.specs2.matcher.XmlMatchers._
import org.specs2.mutable._

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util._

import Formality._

class FieldSpec extends Specification {
  val templateElement = <div class="boomdayada boomdayadan" data-test-attribute="bam" value="markup-value">Here's a test!</div>

  "Simple fields with no initial value" should {
    "only bind the name attribute" in new SScope {
      val formField = field[String](".boomdayada")

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "div",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "value" -> "markup-value",
        "name" -> ".*"
      )
    }
  }
  
  "Simple fields with an initial value" should {
    "only bind the name and value attributes" in new SScope {
      val formField = field[String](".boomdayada", "Dat value")

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "div",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "name" -> ".*",
        "value" -> "Dat value"
      )
    }
  }

  "Regular file upload fields" should {
    "only bind the name and type attributes" in new SScope {
      val formField = fileUploadField(".boomdayada")

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "div",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "name" -> ".*",
        "type" -> "file"
      )
    }
  }

  "Typed file upload fields" should {
    "only bind the name and type attributes" in new SScope {
      implicit def fileToObject(fph: FileParamHolder) = Full("boom")

      val formField = typedFileUploadField[String](".boomdayada")

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "div",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "name" -> ".*",
        "type" -> "file"
      )
    }
  }

  "Select object fields with SelectableOptions" should {
    val objects = List(
      SHtml.SelectableOption(new Exception("ohai"), "ohai"),
      SHtml.SelectableOption(new Exception("obai"), "obai"),
      SHtml.SelectableOption(new Exception("slabai"), "slabai")
    )

    "replace the element wholesale with a select element" in new SScope {
      val formField = selectField[Exception](".boomdayada", objects)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "select",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "name" -> ".*"
      )
    }
    "carry any SelectableOption attributes into the resulting options" in new SScope {
      val objects = List(
        SHtml.SelectableOption(new Exception("ohai"), "ohai", ("test" -> "bam")),
        SHtml.SelectableOption(new Exception("obai"), "obai", ("other-test" -> "bam")),
        SHtml.SelectableOption(new Exception("slabai"), "slabai", ("still-other-test" -> "bam"))
      )

      val formField = selectField[Exception](".boomdayada", objects)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <option>ohai</option>,
        "test" -> "bam"
      )
      resultingMarkup must \\(
        <option>obai</option>,
        "other-test" -> "bam"
      )
      resultingMarkup must \\(
        <option>slabai</option>,
        "still-other-test" -> "bam"
      )
    }
    "mark as selected the default object" in new SScope {
      val default = new Exception("ohai")
      val objects = List(
        SHtml.SelectableOption(default, "ohai"),
        SHtml.SelectableOption(new Exception("obai"), "obai"),
        SHtml.SelectableOption(new Exception("slabai"), "slabai")
      )

      val formField = selectField[Exception](".boomdayada", objects, Full(default))

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <option>ohai</option>,
        "selected" -> "selected"
      )
    }
  }
  "Radio select object fields with SelectableOptions" should {
    val objects = List(
      SHtml.SelectableOption(new Exception("ohai"), "ohai"),
      SHtml.SelectableOption(new Exception("obai"), "obai"),
      SHtml.SelectableOption(new Exception("slabai"), "slabai")
    )

    val templateElement =
      <ul class="boomdayada boomdayadan" data-test-attribute="bam">
        <li>
          <label>
            Here's a test!
            <input type="radio" />
          </label>
        </li>
      </ul>

    "only bind to radio buttons and labels in the markup" in new SScope {
      val formField = selectField[Exception]("li", objects, asRadioButtons = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must not have \("select")

      (resultingMarkup \\ "label").zip(objects).foreach {
        case (label, option) =>
          label.text must_== option.label
      }

      val inputs = resultingMarkup \\ "input"

      // They should all have the same name.
      (Set[String]() ++ inputs.map(_ \ "@name").collect { case Group(Seq(Text(name))) => name }).size must_== 1
      // They should all have different values.
      (Set[String]() ++ inputs.map(_ \ "@value").collect { case Group(Seq(Text(value))) => value }).size must_== 3
    }
    "carry any SelectableOption attributes into the resulting radio buttons" in new SScope {
      val objects = List(
        SHtml.SelectableOption(new Exception("ohai"), "ohai", ("test" -> "bam")),
        SHtml.SelectableOption(new Exception("obai"), "obai", ("other-test" -> "bam")),
        SHtml.SelectableOption(new Exception("slabai"), "slabai", ("still-other-test" -> "bam"))
      )

      val formField = selectField[Exception]("li", objects, asRadioButtons = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <input type="radio" />,
        "test" -> "bam"
      )
      resultingMarkup must \\(
        <input type="radio" />,
        "other-test" -> "bam"
      )
      resultingMarkup must \\(
        <input type="radio" />,
        "still-other-test" -> "bam"
      )
    }
    "set the associated label's for attribute when an option has the id attribute set" in new SScope {
      val objects = List(
        SHtml.SelectableOption(new Exception("ohai"), "ohai", ("id" -> "bam")),
        SHtml.SelectableOption(new Exception("obai"), "obai", ("other-test" -> "bam")),
        SHtml.SelectableOption(new Exception("slabai"), "slabai", ("id" -> "boom"))
      )

      val formField = selectField[Exception]("li", objects, asRadioButtons = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        "label",
        "for" -> "bam"
      ) \> "ohai"
      resultingMarkup must \\(
        "label",
        "for" -> "boom"
      ) \> "slabai"
    }
    "mark as selected the default object" in new SScope {
      val default = new Exception("ohai")
      val objects = List(
        SHtml.SelectableOption(default, "ohai"),
        SHtml.SelectableOption(new Exception("obai"), "obai"),
        SHtml.SelectableOption(new Exception("slabai"), "slabai")
      )

      val formField = selectField[Exception]("li", objects, Full(default), asRadioButtons = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      val selectedLabel =
        (resultingMarkup \\ "label") collect {
          case label: Elem if label.label == "label" && label.text == "ohai" =>
            label
        }

      selectedLabel must \(
        <input type="radio" />,
        "selected" -> "selected"
      )
    }
  }
  "Select object fields with tuples" should {
    val objects = List(
      (new Exception("ohai"), "ohai"),
      (new Exception("obai"), "obai"),
      (new Exception("slabai"), "slabai")
    )

    "replace the  element wholesale with a select element" in new SScope {
      val formField = selectField[Exception](".boomdayada", objects)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "select",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "name" -> ".*"
      )
    }
    "mark as selected the default object" in new SScope {
      val default = new Exception("ohai")
      val objects = List(
        (default, "ohai"),
        (new Exception("obai"), "obai"),
        (new Exception("slabai"), "slabai")
      )

      val formField = selectField[Exception](".boomdayada", objects, Full(default))

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <option>ohai</option>,
        "selected" -> "selected"
      )
    }
  }
  "Radio select object fields with tuples" should {
    val objects = List(
      (new Exception("ohai"), "ohai"),
      (new Exception("obai"), "obai"),
      (new Exception("slabai"), "slabai")
    )

    val templateElement =
      <ul class="boomdayada boomdayadan" data-test-attribute="bam">
        <li>
          <label>
            Here's a test!
            <input type="radio" />
          </label>
        </li>
      </ul>

    "only bind to radio buttons and labels in the markup" in new SScope {
      val formField = selectField[Exception]("li", objects, asRadioButtons = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must not have \("select")

      (resultingMarkup \\ "label").zip(objects).foreach {
        case (label, (_, optionLabel)) =>
          label.text must_== optionLabel
      }

      val inputs = resultingMarkup \\ "input"

      // They should all have the same name.
      (Set[String]() ++ inputs.map(_ \ "@name").collect { case Group(Seq(Text(name))) => name }).size must_== 1
      // They should all have different values.
      (Set[String]() ++ inputs.map(_ \ "@value").collect { case Group(Seq(Text(value))) => value }).size must_== 3
    }
    "mark as selected the default object" in new SScope {
      val default = new Exception("ohai")
      val objects = List(
        (default, "ohai"),
        (new Exception("obai"), "obai"),
        (new Exception("slabai"), "slabai")
      )

      val formField = selectField[Exception]("li", objects, Full(default), asRadioButtons = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      val selectedLabel =
        (resultingMarkup \\ "label") collect {
          case label: Elem if label.label == "label" && label.text == "ohai" =>
            label
        }

      selectedLabel must \(
        <input type="radio" />,
        "selected" -> "selected"
      )
    }
  }
  "Select object fields with just an object" should {
    val objects = List(
      "ohai",
      "obai",
      "slabai"
    )

    "replace the  element wholesale with a select element" in new SScope {
      val formField = selectField[String](".boomdayada", objects)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "select",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "name" -> ".*"
      )
    }
    "mark as selected the default object" in new SScope {
      val formField = selectField[String](".boomdayada", objects, Full("ohai"))

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <option>ohai</option>,
        "selected" -> "selected"
      )
    }
  }
  "Radio select object fields with just an object" should {
    val objects = List(
      "ohai",
      "obai",
      "slabai"
    )

    val templateElement =
      <ul class="boomdayada boomdayadan" data-test-attribute="bam">
        <li>
          <label>
            Here's a test!
            <input type="radio" />
          </label>
        </li>
      </ul>

    "only bind to radio buttons and labels in the markup" in new SScope {
      val formField = selectField[String]("li", objects, asRadioButtons = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must not have \("select")

      (resultingMarkup \\ "label").zip(objects).foreach {
        case (label, value) =>
          label.text must_== value
      }

      val inputs = resultingMarkup \\ "input"

      // They should all have the same name.
      (Set[String]() ++ inputs.map(_ \ "@name").collect { case Group(Seq(Text(name))) => name }).size must_== 1
      // They should all have different values.
      (Set[String]() ++ inputs.map(_ \ "@value").collect { case Group(Seq(Text(value))) => value }).size must_== 3
    }
    "mark as selected the default object" in new SScope {
      val formField = selectField[String]("li", objects, Full("ohai"), asRadioButtons = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      val selectedLabel =
        (resultingMarkup \\ "label") collect {
          case label: Elem if label.label == "label" && label.text == "ohai" =>
            label
        }

      selectedLabel must \(
        <input type="radio" />,
        "selected" -> "selected"
      )
    }
  }

  "Multi select object fields with SelectableOptions" should {
    val objects = List(
      SHtml.SelectableOption(new Exception("ohai"), "ohai"),
      SHtml.SelectableOption(new Exception("obai"), "obai"),
      SHtml.SelectableOption(new Exception("slabai"), "slabai")
    )

    "replace the element wholesale with a select element" in new SScope {
      val formField = multiSelectField[Exception](".boomdayada", objects)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "select",
        "multiple" -> "multiple",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "name" -> ".*"
      )
    }
    "carry any SelectableOption attributes into the resulting options" in new SScope {
      val objects = List(
        SHtml.SelectableOption(new Exception("ohai"), "ohai", ("test" -> "bam")),
        SHtml.SelectableOption(new Exception("obai"), "obai", ("other-test" -> "bam")),
        SHtml.SelectableOption(new Exception("slabai"), "slabai", ("still-other-test" -> "bam"))
      )

      val formField = multiSelectField[Exception](".boomdayada", objects)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <option>ohai</option>,
        "test" -> "bam"
      )
      resultingMarkup must \\(
        <option>obai</option>,
        "other-test" -> "bam"
      )
      resultingMarkup must \\(
        <option>slabai</option>,
        "still-other-test" -> "bam"
      )
    }
    "mark as selected the default objects" in new SScope {
      val defaults = List(new Exception("ohai"), new Exception("slabai"))
      val objects = List(
        SHtml.SelectableOption(defaults(0), "ohai"),
        SHtml.SelectableOption(new Exception("obai"), "obai"),
        SHtml.SelectableOption(defaults(1), "slabai")
      )

      val formField = multiSelectField[Exception](".boomdayada", objects, defaults)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <option>ohai</option>,
        "selected" -> "selected"
      )
      resultingMarkup must \\(
        <option>slabai</option>,
        "selected" -> "selected"
      )
    }
  }
  "Checkbox multi select object fields with SelectableOptions" should {
    val objects = List(
      SHtml.SelectableOption(new Exception("ohai"), "ohai"),
      SHtml.SelectableOption(new Exception("obai"), "obai"),
      SHtml.SelectableOption(new Exception("slabai"), "slabai")
    )

    val templateElement =
      <ul class="boomdayada boomdayadan" data-test-attribute="bam">
        <li>
          <label>
            Here's a test!
            <input type="checkbox" />
          </label>
        </li>
      </ul>

    "only bind to checkboxes and labels in the markup" in new SScope {
      val formField = multiSelectField[Exception]("li", objects, asCheckboxes = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must not have \("select")

      (resultingMarkup \\ "label").zip(objects).foreach {
        case (label, option) =>
          label.text must_== option.label
      }

      val inputs = resultingMarkup \\ "input"

      // They should all have the same name.
      (Set[String]() ++ inputs.map(_ \ "@name").collect { case Group(Seq(Text(name))) => name }).size must_== 1
      // They should all have different values.
      (Set[String]() ++ inputs.map(_ \ "@value").collect { case Group(Seq(Text(value))) => value }).size must_== 3
    }
    "carry any SelectableOption attributes into the resulting checkboxes" in new SScope {
      val objects = List(
        SHtml.SelectableOption(new Exception("ohai"), "ohai", ("test" -> "bam")),
        SHtml.SelectableOption(new Exception("obai"), "obai", ("other-test" -> "bam")),
        SHtml.SelectableOption(new Exception("slabai"), "slabai", ("still-other-test" -> "bam"))
      )

      val formField = multiSelectField[Exception]("li", objects, asCheckboxes = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <input type="checkbox" />,
        "test" -> "bam"
      )
      resultingMarkup must \\(
        <input type="checkbox" />,
        "other-test" -> "bam"
      )
      resultingMarkup must \\(
        <input type="checkbox" />,
        "still-other-test" -> "bam"
      )
    }
    "set the associated label's for attribute when an option has the id attribute set" in new SScope {
      val objects = List(
        SHtml.SelectableOption(new Exception("ohai"), "ohai", ("id" -> "bam")),
        SHtml.SelectableOption(new Exception("obai"), "obai", ("other-test" -> "bam")),
        SHtml.SelectableOption(new Exception("slabai"), "slabai", ("id" -> "boom"))
      )

      val formField = multiSelectField[Exception]("li", objects, asCheckboxes = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        "label",
        "for" -> "bam"
      ) \> "ohai"
      resultingMarkup must \\(
        "label",
        "for" -> "boom"
      ) \> "slabai"
    }
    "mark as selected the default objects" in new SScope {
      val defaults = List(new Exception("ohai"), new Exception("slabai"))
      val objects = List(
        SHtml.SelectableOption(defaults(0), "ohai"),
        SHtml.SelectableOption(new Exception("obai"), "obai"),
        SHtml.SelectableOption(defaults(1), "slabai")
      )

      val formField = multiSelectField[Exception]("li", objects, defaults, asCheckboxes = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      val selectedLabels =
        (resultingMarkup \\ "label") collect {
          case label: Elem if label.label == "label" && (label.text == "ohai" || label.text == "slabai") =>
            label
        }

      selectedLabels.length must_== 2

      selectedLabels(0) must \(
        <input type="checkboxes" />,
        "selected" -> "selected"
      )
      selectedLabels(1) must \(
        <input type="checkboxes" />,
        "selected" -> "selected"
      )
    }
  }
  "Multi select object fields with tuples" should {
    val objects = List(
      (new Exception("ohai"), "ohai"),
      (new Exception("obai"), "obai"),
      (new Exception("slabai"), "slabai")
    )

    "replace the  element wholesale with a select element" in new SScope {
      val formField = multiSelectField[Exception](".boomdayada", objects)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "select",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "name" -> ".*"
      )
    }
    "mark as selected the default objects" in new SScope {
      val defaults = List(new Exception("ohai"), new Exception("slabai"))
      val objects = List(
        (defaults(0), "ohai"),
        (new Exception("obai"), "obai"),
        (defaults(1), "slabai")
      )

      val formField = multiSelectField[Exception](".boomdayada", objects, defaults)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <option>ohai</option>,
        "selected" -> "selected"
      )
      resultingMarkup must \\(
        <option>slabai</option>,
        "selected" -> "selected"
      )
    }
  }
  "Checkbox multi select object fields with tuples" should {
    val objects = List(
      (new Exception("ohai"), "ohai"),
      (new Exception("obai"), "obai"),
      (new Exception("slabai"), "slabai")
    )

    val templateElement =
      <ul class="boomdayada boomdayadan" data-test-attribute="bam">
        <li>
          <label>
            Here's a test!
            <input type="checkbox" />
          </label>
        </li>
      </ul>

    "only bind to checkboxes and labels in the markup" in new SScope {
      val formField = multiSelectField[Exception]("li", objects, asCheckboxes = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must not have \("select")

      (resultingMarkup \\ "label").zip(objects).foreach {
        case (label, (_, optionLabel)) =>
          label.text must_== optionLabel
      }

      val inputs = resultingMarkup \\ "input"

      // They should all have the same name.
      (Set[String]() ++ inputs.map(_ \ "@name").collect { case Group(Seq(Text(name))) => name }).size must_== 1
      // They should all have different values.
      (Set[String]() ++ inputs.map(_ \ "@value").collect { case Group(Seq(Text(value))) => value }).size must_== 3
    }
    "mark as selected the default objects" in new SScope {
      val defaults = List(new Exception("ohai"), new Exception("slabai"))
      val objects = List(
        (defaults(0), "ohai"),
        (new Exception("obai"), "obai"),
        (defaults(1), "slabai")
      )

      val formField = multiSelectField[Exception]("li", objects, defaults, asCheckboxes = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      val selectedLabels =
        (resultingMarkup \\ "label") collect {
          case label: Elem if label.label == "label" && (label.text == "ohai" || label.text == "slabai") =>
            label
        }

      selectedLabels.length must_== 2

      selectedLabels(0) must \(
        <input type="checkboxes" />,
        "selected" -> "selected"
      )
      selectedLabels(1) must \(
        <input type="checkboxes" />,
        "selected" -> "selected"
      )
    }
  }
  "Multi select object fields with just an object" should {
    val objects = List(
      "ohai",
      "obai",
      "slabai"
    )

    "replace the  element wholesale with a select element" in new SScope {
      val formField = multiSelectField[String](".boomdayada", objects)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "select",
        "class" -> "boomdayada boomdayadan",
        "data-test-attribute" -> "bam",
        "name" -> ".*"
      )
    }
    "mark as selected the default objects" in new SScope {
      val formField = multiSelectField[String](".boomdayada", objects, List("ohai", "slabai"))

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \\(
        <option>ohai</option>,
        "selected" -> "selected"
      )
      resultingMarkup must \\(
        <option>slabai</option>,
        "selected" -> "selected"
      )
    }
  }
  "Checkbox multi select object fields with just an object" should {
    val objects = List(
      "ohai",
      "obai",
      "slabai"
    )

    val templateElement =
      <ul class="boomdayada boomdayadan" data-test-attribute="bam">
        <li>
          <label>
            Here's a test!
            <input type="checkbox" />
          </label>
        </li>
      </ul>

    "only bind to radio buttons and labels in the markup" in new SScope {
      val formField = multiSelectField[String]("li", objects, asCheckboxes = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must not have \("select")

      (resultingMarkup \\ "label").zip(objects).foreach {
        case (label, value) =>
          label.text must_== value
      }

      val inputs = resultingMarkup \\ "input"

      // They should all have the same name.
      (Set[String]() ++ inputs.map(_ \ "@name").collect { case Group(Seq(Text(name))) => name }).size must_== 1
      // They should all have different values.
      (Set[String]() ++ inputs.map(_ \ "@value").collect { case Group(Seq(Text(value))) => value }).size must_== 3
    }
    "mark as selected the default objects" in new SScope {
      val formField = multiSelectField[String]("li", objects, List("ohai", "slabai"), asCheckboxes = true)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      val selectedLabels =
        (resultingMarkup \\ "label") collect {
          case label: Elem if label.label == "label" && (label.text == "ohai" || label.text == "slabai") =>
            label
        }

      selectedLabels.length must_== 2

      selectedLabels(0) must \(
        <input type="checkboxes" />,
        "selected" -> "selected"
      )
      selectedLabels(1) must \(
        <input type="checkboxes" />,
        "selected" -> "selected"
      )
    }
  }

  "Checkbox fields with Boolean values" should {
    "replace the element with a checkbox-hidden input pair" in new SScope {
      val formField = checkboxField(".boomdayada", false)

      val resultingMarkup = <test-parent>{formField.binder(templateElement)}</test-parent>

      resultingMarkup must \(
        "input",
        "type" -> "checkbox",
        "name" -> ".*"
      )
      resultingMarkup must \(
        "input",
        "type" -> "hidden",
        "name" -> ".*"
      )

      def nameForType(fieldType: String) = {
        resultingMarkup \ "input" collectFirst {
          case e: Elem if e.attribute("type") == Some(Text(fieldType)) =>
            e.attribute("name").map(_.text)
        } getOrElse {
          None
        }
      }

      nameForType("checkbox") must_== nameForType("hidden")
    }
  }
}
