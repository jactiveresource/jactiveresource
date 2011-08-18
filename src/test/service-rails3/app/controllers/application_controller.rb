class ApplicationController < ActionController::Base
  protect_from_forgery
  layout 'application'

  private
    def authenticate
      authenticate_or_request_with_http_basic do |id, password|
        id == "Ace" && password == "newenglandclamchowder"
      end
    end

end
