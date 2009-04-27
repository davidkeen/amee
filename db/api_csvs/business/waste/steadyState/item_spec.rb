AMEE_SVN_ROOT=ENV['AMEE_SVN']
TEST_ROOT=AMEE_SVN_ROOT+"/internal/tests/api"
require TEST_ROOT+'/spec_helper.rb'

folder=folder_category(__FILE__)

describe folder do
  it_should_behave_like "V2 XML profile"
  it "should give the right amount of carbon" do
    check_calculation(folder,
                      "type=normal",
                      2.0*100*0.72*21*0.25*0.9*(1-2.71828**(-20*0.03)),
                      :wasteDepositionRate => 2.0,
                      :timeSinceClosed=>0,
                      :timeSinceOpened => 20)
  end
end
