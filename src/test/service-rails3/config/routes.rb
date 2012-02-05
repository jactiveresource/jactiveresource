ServiceRails3::Application.routes.draw do
  resources :posts, :comments, :people
end
