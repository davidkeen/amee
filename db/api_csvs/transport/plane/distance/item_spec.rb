AMEE_SVN_ROOT=ENV['AMEE_SVN']
TEST_ROOT=AMEE_SVN_ROOT+"/internal/tests/api"
require TEST_ROOT+'/spec_helper.rb'

folder=folder_category(__FILE__)

describe folder do
  it_should_behave_like "V2 XML profile"
  it "should give the right amount of carbon" do
    check_calculation(folder,
                      "",
                      (0.10462818336163*2.0+34.0680611205433)*2.0,
                      :distance => 2.0,
                      :passengers => 2.0)
  end
end
