/*
 * This file is part of RuneSource.
 *
 * RuneSource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RuneSource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RuneSource.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * A sample Plugin implementation to show how it should be done.
 * 
 * @author Blake Beaupain
 */
public class SamplePlugin implements Plugin {

	@Override
	public void cycle() throws Exception {
		// Called every 600ms when the server engine cycles.
	}

	@Override
	public void onEnable() throws Exception {
		// Called when the plugin is enabled.
	}

	@Override
	public void onDisable() throws Exception {
		// Called when the plugin is disabled.
	}

}
