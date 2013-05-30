require "jsduck/tag/boolean_tag"

class Ametys < JsDuck::Tag::BooleanTag
  def initialize
    @pattern = "ametys"
    @signature = {:long => "Ametys", :short => "Ame"}
    super
  end
end