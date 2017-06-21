# this provides just enough of Rugged to make Licensee::GitProject.new()
# throw an error
module Rugged
	class OSError < ArgumentError; end
	class RepositoryError < ArgumentError; end

	class Repository
		def initialize(path, **args)
			raise OSError, "this is not a very rugged implementation"
		end
	end
end
