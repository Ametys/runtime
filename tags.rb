require "jsduck/tag/boolean_tag"

class Ametys < JsDuck::Tag::BooleanTag
  def initialize
    @pattern = "ametys"
    @signature = {:long => "ametys", :short => "ametys"}
    super
  end
end